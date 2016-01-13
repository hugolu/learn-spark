## checkpoint

用法
```
def checkpoint()
```

範例
```
scala> sc.setCheckpointDir("my_directory_name")
scala> val a = sc.parallelize(1 to 4)
scala> a.checkpoint
scala> a.count
16/01/13 13:42:29 INFO rdd.ReliableRDDCheckpointData: Done checkpointing RDD 0 to hdfs://localhost:9000/user/hadoop/my_directory_name/b49599a7-7543-420f-abc9-a9c4f7e8310d/rdd-0, new parent is RDD 1
res2: Long = 4
```
 - 執行```setCheckpointDir()```會在hdfs裡面產生一個目錄
 - 執行```count()```會在這個目錄產生資料(binary file)
 - 根據說明 checkpointed RDD 會在每次呼叫 action function 儲存運算過程，是用來除錯的？ (待證實)
