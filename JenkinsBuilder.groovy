  def username = ""
  def environment = ""
  def gitCommitHash = ""
  def dockerImage = ""
  def repositoryName = "${JOB_NAME}"
  def registry = "fuchicorp/${repositoryName}"
  def registryCredentials = 'docker-hub-creds'

  def branch = "${scm.branches[0].name}".replaceAll(/^\*\//, '')
  if (branch =~ '^v[0-9].[0-9]' || branch =~ '^v[0-9][0-9].[0-9]' ) {
        // if Application release or branch starts with v* example v0.1 will be deployed to prod
        environment = 'prod' 
        repositoryName = repositoryName + '-prod'
  }

def k8slabel = "jenkins-pipeline-${UUID.randomUUID().toString()}"
def slavePodTemplate = """
      metadata:
        labels:
          k8s-label: ${k8slabel}
        annotations:
          jenkinsjoblabel: ${env.JOB_NAME}-${env.BUILD_NUMBER}
      spec:
        affinity:
          podAntiAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchExpressions:
                - key: component
                  operator: In
                  values:
                  - jenkins-jenkins-master
              topologyKey: "kubernetes.io/hostname"
        containers:
        - name: docker
          image: docker:latest
          imagePullPolicy: IfNotPresent
          command:
          - cat
          tty: true
          volumeMounts:
            - mountPath: /var/run/docker.sock
              name: docker-sock
        serviceAccountName: default
        securityContext:
          runAsUser: 0
          fsGroup: 0
        volumes:
          - name: docker-sock
            hostPath:
              path: /var/run/docker.sock
    """

  properties([
          parameters([
                  booleanParam(defaultValue: false,description: 'Click this if you would like to deploy to latest',name: 'PUSH_LATEST'
          )])
  ])

    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: false) {
      node(k8slabel) {
        stage('Pull SCM') {
            git branch: 'master', credentialsId: 'github-common-access', url: 'https://github.com/fuchicorp/buildtools.git'
            gitCommitHash = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
        }
        dir('Docker/') {
          container("docker") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "docker-hub-creds", usernameVariable: 'username', passwordVariable: 'password']]) {
              stage("Docker Build") {
                dockerImage = docker.build registry
              }

              stage("Docker Login") {
                sh "docker login --username ${env.username} --password ${env.password}"
              }
              stage("Docker Push") {
                docker.withRegistry( '', registryCredentials ) {
                  dockerImage.push("${gitCommitHash}")
                  if (params.PUSH_LATEST) {
                    dockerImage.push("latest")
                  }
                }
              }
            }
          }
        }
      }
    }
