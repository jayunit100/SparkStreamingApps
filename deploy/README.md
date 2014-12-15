== Deployment instructions ==

This repo separates docker instances into microservices.

# Using vagrant

The simplest way to run these is using vagrant.

Vagrantfile in this repo will spin up an OS which can run containers, and will launch each of the containers in it.

# Manually 

One service is cassandra/ The other is in spark/
Each service will start n containers.

- Start both independent services

- Now, run the third docker container, test/, which 
runs the smoke test.
