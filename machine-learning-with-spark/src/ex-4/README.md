## Run/execute the docker container @host
```
$ export MYHOME=$HOME/github/learn-spark
$ docker run --name spark -v $MYHOME:/learn-spark -e "MYHOME=/learn-spark" -d gettyimages/spark
$ docker exec -it spark bash
```

## Run spark-sumit @docker
```
# cd $MYHOME
# spark-submit --class RecommendationApp --jars jars/jblas-1.2.4.jar machine-learning-with-spark/src/ex-4/target/scala-2.11/recommendation-app_2.11-1.0.jar
```
