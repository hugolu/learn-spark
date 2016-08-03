# Hadoop HDFS 命令

命令 | 說明 
-----|-----
`-mkdir [-p] <path> ...`                    | Create a directory in specified location.
`-ls [-R] [<path> ...]`                     | List the contents that match the specified file pattern.
`-put [-f] <localsrc> ... <dst>`            | Copy files from the local file system into fs.
`-copyFromLocal [-f] <localsrc> ... <dst>`  | Identical to the -put command.
`-get <src> ... <localdst>`                 | Copy files that match the file pattern <src> to the local name.
`-copyToLocal <src> ... <localdst>`         | Identical to the -get command.
`-cat <src> ...`                            | Fetch all files that match the file pattern <src> and display their content on stdout.
`-cp [-f] <src> ... <dst>`                  | Copy files that match the file pattern <src> to a destination.
`-rm [-f] [-r|-R] <src> ...`                | Delete all files that match the specified file pattern.

## 建立、查看目錄

### 建立目錄
```shell
$ hadoop fs -mkdir /user
$ hadoop fs -mkdir /user/hduser
$ hadoop fs -mkdir /user/hduser/test
$ hadoop fs -mkdir -p /dir1/dir2/dir3
```

### 查看目錄
```shell
$ hadoop fs -ls /
$ hadoop fs -ls /user
$ hadoop fs -ls /user/hduser
$ hadoop fs -ls -R /
$ hadoop fs -ls # check hduser's home directory
```

## 從本機複製檔案到 HDFS

### 使用 `copyFromLocal`
```shell
$ hadoop fs -copyFromLocal /usr/local/hadoop/README.txt /user/hduser/test
$ hadoop fs -copyFromLocal /usr/local/hadoop/README.txt /user/hduser/test/test1.txt
$ hadoop fs -ls /user/hduser/test
$ hadoop fs -cat /user/hduser/test/README.txt

$ hadoop fs -copyFromLocal -f /usr/local/hadoop/README.txt /user/hduser/test  # -f: overwrite
$ hadoop fs -copyFromLocal /usr/local/hadoop/NOTICE.txt /usr/local/hadoop/LICENSE.txt /user/hduser/test # copy multiple files

$ hadoop fs -copyFromLocal /usr/local/hadoop/etc /user/hduser/test  # copy a directory
$ hadoop fs -ls -R /user/hduser/test/etc  # -R: Recursively list the contents of directories.
```

### 使用 `-put`
```shell
$ hadoop fs -put /usr/local/hadoop/README.txt /user/hduser/test/test2.txt
$ echo hello world | hadoop fs -put - /user/hduser/test/test3.txt # file's content from stdin
```

## 將 HDFS 檔案複製到本機

### 使用 `-copyToLocal`
```shell
$ hadoop fs -copyToLocal /user/hduser/test/README.txt # copy a file
$ hadoop fs -copyToLocal /user/hduser/test/etc        # copy a directory
```

### 使用 `-get`
```shell
$ hadoop fs -get /user/hduser/test/README.txt localREADME.txt # copy a file
$ hadoop fs -get /user/hduser/test/etc etc2                   # copy a directory
```
