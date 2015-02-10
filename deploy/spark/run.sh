#/bin/bash
echo "starting ........"
if [ $1 = "master" ] ; then 
    /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-master.sh ; 
else
    echo "pinging slave........"
    ping -c 2 scale1.docker && /opt/spark-1.2.0-bin-hadoop2.4/sbin/start-slave.sh -h spark://scale1.docker:7077 ; 
fi 

tail -f /opt/spark-1.2.0-bin-hadoop2.4/logs/*
