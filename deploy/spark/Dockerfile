
FROM centos:centos7
RUN yum clean all
RUN yum install -y yum-utils
RUN yum-config-manager --save --setopt=fedora.skip_if_unavailable=true     
RUN yum update -y
RUN yum install -y wget

#### RUN wget -O /etc/yum.repos.d/bigtop.repo http://bigtop01.cloudera.org:8080/view/Releases/job/Bigtop-0.8.0/label=centos6/6/artifact/output/bigtop.repo

#### CDH5 is ahead of bigtop, use this for now.
RUN wget -O /etc/yum.repos.d/cdh5.repo http://archive.cloudera.com/cdh5/redhat/6/x86_64/cdh/cloudera-cdh5.repo -O /etc/yum.repos.d/cdh5.repo  

#### Now install SSH so we can layer in the spark components.
RUN yum -y install openssh-server openssh-clients sudo
RUN sed -i.bak s/UsePAM\ yes/UsePAM\ no/ /etc/ssh/sshd_config
RUN ssh-keygen -q -N "" -t dsa -f /etc/ssh/ssh_host_dsa_key
RUN ssh-keygen -q -N "" -t rsa -f /etc/ssh/ssh_host_rsa_key
# requiretty off
RUN sed -i.bak 's/requiretty/!requiretty/' /etc/sudoers
# setup vagrant account
RUN mkdir /root/.ssh/
RUN chmod 0755 /root/.ssh
RUN wget http://github.com/mitchellh/vagrant/raw/master/keys/vagrant.pub --no-check-certificate -O /root/.ssh/authorized_keys
RUN chmod 0644 /root/.ssh/authorized_keys
CMD /usr/sbin/sshd -D
