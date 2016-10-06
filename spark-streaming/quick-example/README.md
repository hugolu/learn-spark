## Socket Streaming

### 資料產生端

#### 透過 socket 產生資料
```shell
$ python socketSource.py
```
- 在 localhost:9999 等待連線，連線後寫出資料
- 資料內容依次為 "A B C D", "B C D E", "C D E F", "D E F A", "E F A B", "F A B C"，重複循環

#### 透過 file 產生資料
```shell
$ python fileSource.py
```
- 在 wordCount 目錄下，產生 file-????.txt 的檔案
- 檔案內容依次為 "A B C D", "B C D E", "C D E F", "D E F A", "E F A B", "F A B C"，重複循環

### 資料處理端

#### Socket Streaming
```shell
$ spark-submit --class SocketWordCount target/scala-2.11/spark-streaming-app_2.11-1.0.jar
```

#### File Streaming
```shell
$ spark-submit --class FileWordCount target/scala-2.11/spark-streaming-app_2.11-1.0.jar
```

#### Queue Streaming
```shell
$ spark-submit --class QueueWordCount target/scala-2.11/spark-streaming-app_2.11-1.0.jar
```

#### Stateful Streaming
```shell
$ spark-submit --class StatefulWordCount target/scala-2.11/spark-streaming-app_2.11-1.0.jar
```

#### Streaming Window
```shell
spark-submit --class WindowWordCount target/scala-2.11/spark-streaming-app_2.11-1.0.jar
```
