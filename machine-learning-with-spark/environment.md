# 實驗環境

> 計畫用 spark on docker 來建立實驗環境，一來方便、二來效率較VM高

```shell
$ docker run --name spark -v $HOME/learn-spark:/learn-spark -d gettyimages/spark
```

```shell
$ docker exec -it spark bash
```
