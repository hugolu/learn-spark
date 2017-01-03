# Environment

## Source code

### Create the working directory @host
```
$ mkdir -p $HOME/github
$ git clone https://github.com/hugolu/learn-spark
$ cd $HOME/github/learn-spark
```

### Compile the source @host
```
$ export MYHOME=$HOME/github/learn-spark
$ cd $MYHOME/spark-on-docker
$ sbt compile
$ sbt package
```

### Sync the code @host
```
$ git pull
$ git push
```

## Spark on Docker

### Download docker image @host
```
$ docker pull gettyimages/spark:2.0.2-hadoop-2.7
$ docker tag gettyimages/spark:2.0.2-hadoop-2.7 gettyimages/spark:latest
```

### Run/execute the docker container @host
```
$ export MYHOME=$HOME/github/learn-spark
$ docker run --name spark -v $MYHOME:/learn-spark -e "MYHOME=/learn-spark" -d gettyimages/spark
$ docker exec -it spark bash
```

### Run spark-sumit @docker
```
# cd $MYHOME
# spark-submit --class WordCount $MYHOME/spark-on-docker/target/scala-2.11/spark-on-docker_2.11-1.0.0.jar
```
