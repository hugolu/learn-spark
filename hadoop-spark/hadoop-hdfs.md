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
