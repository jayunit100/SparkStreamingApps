== Deployment instructions ==

This repo separates docker instances into microservices.

# Overview

This repository is a WIP that spins up spark and cassandra microservices and 
attempts to orchestrate them.

CURRENT STATUS : Once you run ```vagrant up```, a spark cluster is created, and the registration of master and slave can be seen by running ```vagrant docker-logs```.

The simplest way to run these is using vagrant.

Alternatively, you can use the individual Dockerfiles and modify them - they are proper microservices , or at least, they should be :)

# Instructions.

BEFORE running, however, run ```setup.sh```  

This is necessary to cache the Tarballs efficiently.
Also, this script builds the base Dockerfile (JDK and nothing else).

# Testing

once started you can do some basic tests.

- Find the spark master container (there will be 3, you can find it by running jps on each).  Then run this
```docker exec -i -t f0ab916b0f88 /opt/spark-1.2.0-bin-hadoop2.4/bin/spark-submit --class org.apache.spark.examples.SparkPi --master spark://scale1.docker:7077 /scale-shared/spark-examples_2.10-1.1.1.jar 100```

- Testing cassandra from the spark slaves can also be easily done, find the spark slave (again , just run jps in each container), and then run.

```/opt/apache-cassandra.../bin/cassandra-cli``` followed by ```connect cassandra1.docker/9160``` .  The connection should succeed, and then you can do things like create keyspaces etc.



