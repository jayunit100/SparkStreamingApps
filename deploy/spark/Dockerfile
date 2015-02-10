FROM jvm
RUN yum clean all
RUN yum install -y tar yum-utils wget
RUN yum-config-manager --save --setopt=fedora.skip_if_unavailable=true     
RUN yum update -y

# Java
RUN yum install -y java-1.7.0-openjdk-devel.x86_64

# Spark  in /opt. default to hadoop 2.4 version.
COPY spark-1.2.0-bin-hadoop2.4.tgz /opt/
# ADD from HTTP doesn't unzip for us (https://github.com/docker/docker/issues/6353).  So untar here.
RUN tar -xzf /opt/spark-1.2.0-bin-hadoop2.4.tgz -C /opt/
RUN echo "SPARK_HOME=/opt/spark-1.2.0-bin-hadoop2.4" >> /etc/environment

#RUN echo "172.17.0.2	scale1.docker" >> /etc/hosts
#RUN echo "172.17.0.3	scale2.docker" >> /etc/hosts

RUN echo "JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk/" >> /opt/spark-1.2.0-bin-hadoop2.4/conf/spark-env.sh

### One dockerfile : Use hostname to decide wether or not slave of master... 
#CMD ls

#CMD echo "XXXXXX" && cat /etc/hosts && echo "YYYY" && ip addr | grep 17 && hostname && if [[ `hostname` = 'scale1.docker' ]] ; then /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh ; else ping -c 2 scale1.docker && /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-slave.sh -h spark://scale1.docker:7077 ; fi ; tailf /opt/spark-1.2.0-bin-hadoop2.4/logs/*

ADD run.sh /opt/run.sh

CMD echo `hostname` && if [[ `hostname` = 'scale1.docker' ]] ; then /opt/run.sh master ; else /opt/run.sh slave ;  fi
# CMD /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh
