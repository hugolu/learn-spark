# 探索與可視化數據

參考資料
- [Machine Learning with Spark - Tackle Big Data with Powerful Spark Machine Learning Algorithms](https://www.amazon.com/Machine-Learning-Spark-Powerful-Algorithms/dp/1783288515)
- [在 Spark 中使用 IPython Notebook](http://www.cnblogs.com/NaughtyBaby/p/5469469.html)
- [ipythonnotebook + spark](http://blog.csdn.net/sadfasdgaaaasdfa/article/details/47090513)
- [Spark入门（Python版）](http://blog.jobbole.com/86232/)

## 安裝、設定

### 下載、安裝 IPython Notebook
下載預先編譯好的 python 套件 [Anacaonda](https://www.continuum.io/downloads)

```shell
$ wget http://repo.continuum.io/archive/Anaconda2-4.1.1-Linux-x86_64.sh
$ bash Anaconda2-4.1.1-Linux-x86_64.sh
```

### 設定環境
根據 [在 Spark 中使用 IPython Notebook](http://www.cnblogs.com/NaughtyBaby/p/5469469.html) 說明，修改 `~/.bashrc` 設定環境變數

```
# pyspark variables
export PYSPARK_DRIVER_PYTHON=ipython2
export PYSPARK_DRIVER_PYTHON_OPTS="notebook --ip=192.168.33.10"
```
- VM private network IP=192.168.33.10，要將 Guest OS Web service 綁定這個 IP，Host OS 的瀏覽器才能使用 IPython Notebook

```shell
$ source ~/.bashrc
```

### 啟動 PySpark with IPython Notebook
```shell
$ pyspark
```

### 牛刀小試

### 停止 PySpark with IPython Notebook
- 在 terminal 按下 `q`，出現提示 `Do you want to exit w3m? (y/n)`，回答 `y`
- 然後按下 `Ctrl-C` 停止伺服器，出現提示 `Shutdown this notebook server (y/[n])?`，回答 `y`

