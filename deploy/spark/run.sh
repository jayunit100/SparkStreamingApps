#/bin/bash
    
if [ $# -eq 0 ]; then
        echo "No arguments supplied"
	echo "USAGE ./run.sh master|slave"
        exit 1
fi   
 
echo "running docker spark startup"

if [ $1 = "master" ] ; then 
        /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh ; 
# slave
else
        ping -c 2 scale1.docker && /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-slave.sh -h spark://scale1.docker:7077 ;  
fi
### In all cases, we tail the logs as the final task...
tailf /opt/spark-1.2.0-bin-hadoop2.4/logs/*
