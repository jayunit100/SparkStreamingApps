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



