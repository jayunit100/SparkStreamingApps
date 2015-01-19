#/bin/bash
    
if [ $# -eq 0 ]; then
        echo "No arguments supplied"
	echo "USAGE ./run.sh master|slave"
        exit 1
fi   
 
### Vagrant : This is untested, but it should generally work.
if [[ `hostname -s` =~ scale[0-9]*docker ]]; then
    if [ $1 = "master" ] ; then 
        /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh ; 
    # slave
    else
        ping -c 2 scale1.docker && /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-slave.sh -h spark://scale1.docker:7077 ; 
    fi 

### Kubernetes
else
   
    # One arg expected when launching kubernetes (master or slave)
    if [ $1 = "slave" ]; then 
       echo "START SLAVE pointing to ${SPARK_MASTER_SERVICE_HOST} " 
       /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-slave.sh -h spark://${SPARK_MASTER_SERVICE_HOST}:7077
    # master
    else 
       /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh
   
    fi
fi
### In all cases, we tail the logs as the final task...
tailf /opt/spark-1.2.0-bin-hadoop2.4/logs/*
