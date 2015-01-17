#/bin/bash
### Vagrant : This is untested, but it should generally work.
if [[ `hostname -s` =~ scale[0-9]*docker ]]; then
    if [[ `hostname` = 'scale1.docker' ]] ; then 
        /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh ; 
    else 
        ping -c 2 scale1.docker && /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-slave.sh -h spark://scale1.docker:7077 ; 
    fi 
### Kubernetes
else
    if [ -n "${SPARK_MASTER_HOST+1}" ]; then
       echo "START SLAVE pointing to ${SPARK_MASTER_HOST} " 
       /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-slave.sh -h spark://${SPARK_MASTER_HOST}:7077        
    else 
       /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh
    fi
fi
### In all cases, we tail the logs as the final task...
tailf /opt/spark-1.2.0-bin-hadoop2.4/logs/*
