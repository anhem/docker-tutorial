# Docker - hands on tutorial

Docker is a container environment that runs on top of the Linux Kernel. 

It mainly consists of instructions, layers, images and containers. 

* An instruction is a command defined in a Dockerfile. Here are some examples
    * ADD
    * COPY
    * ENV
    * FROM
    * WORKDIR
* A layer is the result of an executed instruction from a Dockerfile
* An image is the sum of all layers created from a Dockerfile. All layers are read-only.
* A container is the last layer on top of an image and is writeable. Multiple containers can be created from the same image.
 
## Installation

install docker for your OS. Linux distros usually get their installation through their package manager.

[Mac installation](https://docs.docker.com/docker-for-mac/install/)

[Windows installation](https://docs.docker.com/docker-for-windows/install/)

## verify that docker is working

To verify that docker is working properly download the docker image `hello-world` from [Docker Hub](https://hub.docker.com/) with
```
$ docker pull hello-world
```
Start a docker container using the downloaded docker image with
```
$ docker run hello-world
```
If working properly `Hello from Docker!` should be displayed along with some other information.

It is also possible to start the docker container directly. The docker image will be downloaded automatically.

## Docker tags (image version/variant)

Tags are used to differentiate between versions/variants of the same image. 
If no tag is specified when starting a container it will use the `latest` image. 
To run a different variant of an image add the tag at the end of the image separated by `:` 
```
$ docker run hello-world:linux
```

## start and run
Once a container has finished executing it will exit. For example a container based on the `hello-world` image will exit as soon as it has displayed its message.

Each time `docker run` is executed a new container is started. We can list all containers that are running or has exited with
```
$ docker ps -a
```
> -a, --all = Show all containers (default shows just running)

Each container is automatically given a name and a container id. The name can also be specified
```
$ docker run --name my_container hello-world
```
> --name string = Assign a name to the container

Instead of creating a new container all the time we can start an existing container using its name or container id
```
$ docker start -i my_container
```
> -i, --interactive = Attach container's STDIN

## Lifecycle
To have a container running without exiting by itself we must make sure it never finishes. So lets build a docker image that does exactly that.

Tailing dev/null is a simple way of making sure a process never finishes by itself.
```
tail -f /dev/null
```
The file [Dockerfile_keep-alive](Dockerfile_keep-alive) has two instructions

* `FROM` defines what docker image we want our own docker image to be based on. In this case Alpine Linux, which is a lightweight Linux distribution
* `ENTRYPOINT` is tailing dev/null

The Dockerfile can be built into an image
```
$ docker build -t keep-alive -f Dockerfile_keep-alive .
```
> -t, --tag list = Name and optionally a tag in the 'name:tag' format

> -f, --file string = Name of the Dockerfile (Default is 'PATH/Dockerfile')

If we list all docker images we can see that we now have two different images. Alpine that we based our image on and our newly created keep-alive image  
```
$ docker images
```
Start a container named `alive` from our docker image
```
$ docker run -d --name alive keep-alive
```
> -d, --detach = Run container in background and print container ID

> --name string = Assign a name to the container

The container should be visible with
```
$ docker ps
```
and stopped with
```
$ docker stop alive
```
If we no longer want our container it can be removed
```
$ docker rm alive
```
and only after the container is removed the image can be removed
```
$ docker rmi keep-alive
```
The alpine image still remains however and may be removed manually.

## Dockerfile

The Dockerfile contains instructions used to create the different layers of a docker image. Most Dockerfiles starts with a parent image as the first layer by declaring a FROM instruction.
[Dockerfile_web-application](Dockerfile_web-application) looks like this
```
FROM ubuntu:18.04
RUN apt-get update
RUN apt-get install -y openjdk-11-jdk
WORKDIR /opt/application
COPY web-application-*.jar web-application.jar
ENTRYPOINT java -jar web-application.jar
```
The first `FROM` instruction tells docker to use an existing docker image from [Docker Hub](https://hub.docker.com/) called `ubuntu` of version/variant `18.04`

Next are two `RUN` instructions executing commands on top of Ubuntu. It tells Ubuntu's package manager `apt-get` to update its list of packages and then install openJDK 11.

The `WORKDIR` is used to change the working directory to `/opt/application`. It it similar to executing `RUN mkdir /opt/application` and `RUN cd /opt/application` but we only need one instruction and the directory persists between all succeeding instructions.

`COPY` will copy the web-application jar file into `/opt/application`

The last instruction `ENTRYPOINT` tells docker to start the application.jar

> To be able to run a docker image as an executable container the Dockerfile must have a `CMD` or `ENTRYPOINT` instruction. 
If only one of them is defined in a Dockerfile they have the same effect. However the `CMD` is used to provide default scenarios that can be overridden. They can also coexist in wich case the `CMD` is appended to the `ENTRYPOINT`

As most Dockerfiles starts with a parent image as the first layer, we can split our [Dockerfile_web-application](Dockerfile_web-application) in two separate Dockerfiles. The [first](Dockerfile_ubuntu-openjdk11) could look like this
```
FROM ubuntu:18.04
RUN apt-get update
RUN apt-get install -y openjdk-11-jdk
```
and the [second](Dockerfile_web-application_alt) something like this
```
FROM ubuntu-openjdk-11
WORKDIR /opt/application
COPY web-application-*.jar web-application.jar
ENTRYPOINT java -jar web-application.jar
```
This way the first Dockerfile can be used to create an image shared between images that require ubuntu and openJDK 11.  

## Network and port

Once the docker image is built from [Dockerfile_web-application](Dockerfile_web-application)  it can be started. However, 
to be able to access the endpoint http://localhost:3000/get-date we need to tell docker what port to expose. The web-application is using `8080` but we want to use port `3000`.
```
$ docker build -t web-app -f Dockerfile_web-application .
$ docker run -d --name web-app -p 3000:8080 web-app
```
> -p, --publish list = Publish a container's port(s) to the host

> Note that the docker image and the docker container can have the same name

TODO more on networks
```
$ docker network ls
```

## inspecting and logging

The running web-app container can be inspected with
```
$ docker inspect web-app
```
This gives some low-level information about the container.

We can also take a look at the web-application stdout with
```
$ docker logs web-app
```
If we want to, we can also access the terminal of the running container. 
This is particularly useful when creating a new docker image to sort out issues with the Dockerfile instructions. 
As long as the container is not exiting, by tailing dev/null for example, we can debug and verify that certain instructions work by manually executing them from within the container.
```
$ docker exec -it web-app /bin/bash
```
> -i, --interactive = Keep STDIN open even if not attached

> -t, --tty = Allocate a pseudo-TTY

```
root@802d0eabb638:/opt/application# ls
web-application.jar
```
we can also execute commands directly
```
$ docker exec web-app cat /var/log/web-application/web-application.log
```

## Environment variables
Environment variables can be used either when starting a new container or as an `ENV` instruction in a Dockerfile
```
$ docker run -e DATE_FORMAT=YYYY/MM/dd -d --name web-app -p 3000:8080 web-app
```
> -e, --env list = Set environment variables
docker volume create data_volume

## Persisting data
Any data that is written in a docker container, such as logs, stays in that docker container. If the container is deleted the data is also deleted.
This can however be remedied with the use of bind mounts or volumes.

Using `bind mounts` we can mount a file or directory from the host machine into the container. If the container writes to the mount it will be written to the host filesystem.
```
$ docker run -v /var/log/docker/web-application/:/var/log/web-application/ -d --name web-app -p 3000:8080 web-app
```
> -v, --volume list = Bind mount a volume

and then from the host machine
```
$ cat /var/log/docker/web-application/web-application.log
```
As an alternative, and preferred mechanism for persisting data, `data volumes` can be created for the same purpose.  
```
$ docker volume create web-app-volume
$ docker run --mount source=web-app-volume,target=/var/log/web-application/ -d --name web-app -p 3000:8080 web-app
```
and then from the host machine inspect the volume to get the mountpoint and access the data
```
$ docker volume inspect web-app-volume
$ cat /var/lib/docker/volumes/web-app-volume/_data/web-application.log
```

## Restart policies

Docker has different policies that can be defined during startup to make sure a docker container is always up and running.
```
$ docker run --restart=unless-stopped -d --name web-app -p 3000:8080 web-app
```
> --restart string = Restart policy to apply when a container exits (default "no")

## Docker Compose

Docker Compose is a tool for defining and running multiple containers at once without the need for managing every container individually. 
Here is an example of a docker compose file setting up a TeamCity build server, a TeamCity agent and a postgres database

[docker-build-server](https://github.com/anhem/docker-build-server)

## Other docker commands
* docker history <name>
* docker system prune

## Another Dockerfile example
[Dockerfile](https://github.com/anhem/urchin/blob/master/docker/Dockerfile)