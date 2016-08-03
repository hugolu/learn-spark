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
