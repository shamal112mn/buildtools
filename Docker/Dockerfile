FROM centos:7

LABEL Fuchi Corp <fuchicorpsolution@gmail.com>

## Version of the Docker
ENV DOCKERVERSION=18.03.1-ce

## Installing wget, vim,telnet, git 
RUN yum install wget vim telnet git -y && yum clean all

##Installing helm 
RUN curl https://storage.googleapis.com/kubernetes-helm/helm-v2.14.0-linux-amd64.tar.gz > ./helm.tar.gz \
    && tar -xvf ./helm.tar.gz \
    && mv ./linux-amd64/*  /usr/local/bin \
    && rm helm.tar.gz

## Installing terraform
RUN wget https://releases.hashicorp.com/terraform/0.11.14/terraform_0.11.14_linux_amd64.zip \
    && yum install unzip -y \
    && unzip terraform_0.11.14_linux_amd64.zip \
    && rm -rf  terraform_0.11.14_linux_amd64.zip \
    && mv terraform /bin

## Download docker cli
RUN curl -fsSLO https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKERVERSION}.tgz \
  && tar xzvf docker-${DOCKERVERSION}.tgz --strip 1 -C /usr/local/bin docker/docker \
  && rm docker-${DOCKERVERSION}.tgz

## Install kubectl
RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.13.0/bin/linux/amd64/kubectl \
    && chmod +x ./kubectl \
    && mv ./kubectl /usr/local/bin/kubectl \
    && mkdir ~/.kube

## Coping the script to set kube config
COPY . /scripts/Dockerfile/

## Running the script to set ~/.kube/config file
CMD su - root -c "sh /scripts/Dockerfile/set-config.sh"
