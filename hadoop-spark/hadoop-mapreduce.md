# Hadoop MapReduce

![](http://blog.trifork.com//wp-content/uploads/2009/08/MapReduceWordCountOverview.png)

術語 | 說明
-----|------
Map     | 將原本文字轉換為 (key, value)，其中 key 是 word，value 是固定是 1
Shuffle | 將相同的 key 排序在一起
Reduce  | 將相同的 value 相加

### 參考連結
- [MapReduce Tutorial](http://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html)
- [第7章. Hadoop MapReduce介紹](http://hadoopspark.blogspot.tw/2016/04/7-hadoop-mapreduce.html)

### 開發步驟
1. 編輯 wordCount.java
2. 編譯 wordCount.java
3. 下載測試文字檔
4. 上傳文字檔到 HDFS
5. 執行 wordCount.java
6. 查看執行結果

## 編輯 wordCount.java 

```shell
$ mkdir -p ~/wordcount/input
$ cd ~/wordcount
$ vi WordCount.java
```

```java
//file: WordCount.java

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
```

## 編譯 wordCount.java

### 設定執行環境
修改 ~/.bashrc，新增以下內容
```
export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
```
```shell
$ source ~/.bashrc
```

### 編譯 WordCount.java
```shell
$ hadoop com.sun.tools.javac.Main WordCount.java
```

### 封裝 WordCount
```shell
$ jar cf wc.jar WordCount*.class
```

> jar (Java ARchive) 是一種壓縮檔，包含 class 與相關資源，後續使用 wc.jar 執行程式

## 產生測試文字檔
```shell
$ vi input/sample.txt
```

sample.txt:
```
apple dog cat
cat apple dog
cat dog cat
```

## 上傳文字檔到 HDFS
```shell
$ hadoop fs -mkdir -p wordcount/input
$ hadoop fs -copyFromLocal input/sample.txt wordcount/input
```

## 執行 wordCount.java
```shell
$ hadoop jar wc.jar WordCount wordcount/input/sample.txt wordcount/output
```

## 查看執行結果
```shell
$ hadoop fs -ls wordcount/output
Found 2 items
-rw-r--r--   3 hduser supergroup          0 2016-08-03 05:58 wordcount/output/_SUCCESS
-rw-r--r--   3 hduser supergroup         20 2016-08-03 05:58 wordcount/output/part-r-00000
$ hadoop fs -cat wordcount/output/part-r-00000
apple	2
cat	4
dog	3
```

## Hadoop MapReduce 缺點
- 不易使用 - MapReduce API 太低階，很難提高開發生產力
- 效率不佳 - 中間結果儲存在硬碟中，會有讀寫延遲的問題
- 批次處理 - 不支援即時處理
