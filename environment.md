# Environment

## Source code

### Create the working directory
```
$ mkdir -p $HOME/github
$ git clone https://github.com/hugolu/learn-spark
$ cd $HOME/github/learn-spark
```

### Sync the code
```
$ git pull
$ git push
```

## Spark on Docker

### Download docker image
```
$ docker pull gettyimages/spark:2.0.2-hadoop-2.7
$ docker tag gettyimages/spark:2.0.2-hadoop-2.7 gettyimages/spark:latest
```

### Run the docker container
```
$ docker run --name spark -v $HOME/github/learn-spark:/learn-spark -d gettyimages/spark
```

### Compile the source
```
$ cd $HOME/github/learn-spark/spark-on-docker
$ sbt compile
$ sbt package
```

### Execute spark-sumit
```
$ docker exec -it spark bash
```
```
$ spark-submit --class WordCount /learn-spark/spark-on-docker/target/scala-2.11/spark-on-docker_2.11-1.0.0.jar
```
