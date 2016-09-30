## 資料源

```shell
$ python alphaGen.py
```

## 處理 Streaming data
```shell
$ spark-submit --class org.apache.spark.examples.streaming.NetworkWordCount target/scala-2.11/spark-streaming-app_2.11-1.0.jar localhost 9999
16/09/30 15:32:32 WARN BlockManager: Block input-0-1475242352600 replicated to only 0 peer(s) instead of 1 peers
...
-------------------------------------------
Time: 1475242355000 ms
-------------------------------------------
(F,3)
(D,4)
(A,4)
(E,3)
(C,7)

16/09/30 15:32:36 WARN BlockManager: Block input-0-1475242355800 replicated to only 0 peer(s) instead of 1 peers
...
-------------------------------------------
Time: 1475242360000 ms
-------------------------------------------
(B,9)
(F,8)
(D,7)
(A,7)
(E,10)
(C,9)

16/09/30 15:32:41 WARN BlockManager: Block input-0-1475242360800 replicated to only 0 peer(s) instead of 1 peers
...
-------------------------------------------
Time: 1475242365000 ms
-------------------------------------------
(B,11)
(F,11)
(D,5)
(A,7)
(E,7)
(C,8)
```
> **replicated to only 0 peer(s) instead of 1 peers** 的 Warn 不知如何移除！？
