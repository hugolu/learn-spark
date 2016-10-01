## Socket Streaming

### 資料產生端

```shell
$ python alphaGen.py
```

### 資料處理端

```shell
$ spark-submit --class SocketStreaming target/scala-2.11/spark-streaming-app_2.11-1.0.jar
-------------------------------------------
Time: 1475328280000 ms
-------------------------------------------
(B,7)
(F,5)
(D,6)
(A,5)
(C,9)
(E,7)

-------------------------------------------
Time: 1475328285000 ms
-------------------------------------------
(B,2)
(F,10)
(D,11)
(A,8)
(C,12)
(E,7)
```

## File Streaming

### 資料產生端
```shell
$ echo "A B C D E F" > wordCount/0001
$ echo "A B C D E F" > wordCount/0002
```

### 資料處理端
```shell
$ spark-submit --class FileStreaming target/scala-2.11/spark-streaming-app_2.11-1.0.jar
-------------------------------------------
Time: 1475328745000 ms
-------------------------------------------
(B,2)
(F,2)
(D,2)
(A,2)
(C,2)
(E,2)
```
