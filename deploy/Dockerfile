FROM centos:centos7
RUN yum clean all
RUN yum install -y tar yum-utils wget
RUN yum-config-manager --save --setopt=fedora.skip_if_unavailable=true     
RUN yum update -y

# Java
RUN yum install -y java-1.7.0-openjdk-devel.x86_64

