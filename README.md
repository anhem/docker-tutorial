# Docker - hands on tutorial

install docker for your OS. Linux distros usually get their installation through its package manager.

[Mac installation](https://docs.docker.com/docker-for-mac/install/)

[Windows installation](https://docs.docker.com/docker-for-windows/install/)

## verify that docker is working

Download the docker image **hello-world** from the [Docker Hub](https://hub.docker.com/) registry.

```
$ docker pull hello-world
```
Start a docker container by using the docker image. If working properly some `Hello from Docker!` should be displayed along with some other information.
```
$ docker run hello-world
```
It is also possible to start the docker container directly. The docker image will be downloaded automatically.

## Docker tags (image version/variant)

Tags are used to differentiate between versions/variants of the same image. 
If no tag is specified when starting a container, the `latest` image will be used. 
To run a different variant of the image add the tag at the end of the image separated by `:` 
```
$ docker run hello-world:linux
```

## start and run
Once a container has finished executing it will exit. The hello-world for example exits as soon as it has displayed its message.

Each time `docker run` is executed a new container is started. We can list all containers that are running or has exited
```
$ docker ps -a
```
> -a, --all = Show all containers (default shows just running)

Each container is automatically given a name and a container id. The name can also be specified
```
$ docker run --name my_container hello-world
```
> --name string = Assign a name to the container

Instead of creating a new container everytime we can start an existing container using its name or container id
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
The file `Dockerfile_keep-alive` has two instructions

* `FROM` defines what docker image we want our docker image to be based on. In this case Alpine Linux, which is a lightweight Linux distribution
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

docker run -p 80:8080 webapp
docker run - v /opt/data:/var/lib/mysql mysql
docker run - e message=hello hello-world
docker rm <name>
docker rmi <image name>
docker exec <name> cat /etc/hosts
docker inspect <name>
docker logs <name>
docker build Dockerfile - t anhem/hello-world
docker push anhem/hello-world
docker history <name>
docker volume create data_volume
docker run - d - -name=vote -p 5000:80 - -link redis:redis voting-app
docker system prune
docker system prune -a