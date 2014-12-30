== Deployment instructions ==

This repo separates docker instances into microservices.

# Using vagrant

The simplest way to run these is using vagrant.

BEFORE running, however, run setup.sh.  This is necessary to cache the Tarballs efficiently.

## Vagrantfile in this repo will spin up an OS which can run containers, and will launch each of the containers in it.
It expects
- Docker will be assigning 172....0 as the FIRST container address (this means: NO other containers running).
- Vagrant and docker both installed
- Running as root.  

# Future

In the future, we'd like to be able to run this project like this.

- Grab the Dockerfiles.
- Add the Vagrantfile modifications into the dockerfile.
- Start M cassandra containers
- Start N spark containers with ability to reach cassandra master.
