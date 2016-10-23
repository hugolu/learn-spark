# Docker 指令範例

## 參考
- [《Docker —— 從入門到實踐》正體中文版](https://www.gitbook.com/book/philipzheng/docker_practice)

## 簡介

Docker 使用 Linux Container (LXC)，實作輕量級的作業系統虛擬化解決方案
- 傳統虛擬機器 - 在硬體層面實作，上面再架設 Guest OS
- Docker 虛擬方式 - 在作業系統層面虛擬化，直接使用本機作業系統

為什麼要用 Docker
- 更快速的交付和部署: make once, deploy anytime
- 更有效率的虛擬化: without hardware emulation
- 更輕鬆的遷移和擴展: just move an image
- 更簡單的管理: incremental updates

特性 | Docker | 虛擬機器
--------|--------|--------
啟動時間 | 秒級 | 分鐘級
儲存容量 | MB | GB
效能 | 接近原生系統 | 較慢
啟動數量 | 數千 | 數十

## 基本概念

概念 | 說明 | 範例
----|----|----
映像檔 (Image) | 一個唯讀的模板 | 包含一個完整的 ubuntu OS，裡面僅安裝了 Apache 應用程式
容器 (Container) | 從映像檔建立的執行實例 | 一個簡易版的 Linux 環境和在其中執行的應用程式)
倉庫 (Repository) | 集中存放映像檔檔案的場所 | 最大公開倉庫 [Docker Hub](https://hub.docker.com/), 中國公開倉庫 [网易蜂巢](https://c.163.com/hub), [时速云](https://hub.tenxcloud.com/)

> 映像檔是唯讀的，容器在啟動的時候建立一層可寫層作為最上層

## 安裝 Docker
- [Docker for Mac](https://docs.docker.com/engine/installation/mac/)
- [Docker for Windows](https://docs.docker.com/engine/installation/windows/)
- [Docker for Ubuntu](https://docs.docker.com/engine/installation/linux/ubuntulinux/)
- [Docker for CentOS](https://docs.docker.com/engine/installation/linux/centos/)

安裝後，執行下面指令確定安裝成功
```shell
$ docker --version
$ docker run hello-world
```

## 映像檔

在執行**容器**前需要本地端存在對應的**映像檔**，如果映像檔不存在本地端，Docker 會從映像檔**倉庫**下載。

### 取得映像檔
```
docker pull <server url>/<namespace>/<repository>:<tag>
```
```shell
$ docker pull ubuntu:12.04
$ docker pull registry.hub.docker.com/ubuntu:12.04
```

### 列出本機映像檔
```shell
$ docker images
```

### 建立映像檔
```shell
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
hello-world         latest              c54a2cc56cbb        3 months ago        1.848 kB
$ docker run -ti hello-world
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS                     PORTS               NAMES
0f0f1f193c69        hello-world         "/hello"            5 seconds ago       Exited (0) 4 seconds ago                       gigantic_swanson
$ docker commit -m "hello-world2" 0f0f1f193c69 hello-world2
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
hello-world2        latest              c0790866bb73        6 seconds ago       1.848 kB
hello-world         latest              c54a2cc56cbb        3 months ago        1.848 kB
```

### 儲存和載入映像檔
```shell
$ docker save -o hello-world.tar hello-world
$ docker load -i hello-world.tar
```

### 移除本地端映像檔
```shell
$ docker rmi hello-world
```

### 映像檔的實作原理

Docker 使用 [Union FS](https://en.wikipedia.org/wiki/UnionFS) 
- 將不同層的增量修改結合到一個映像檔
- 將一個唯讀的分支和一個可寫的分支聯合在一起，在映像檔不變的基礎上允許使用者在其上進行一些寫操作

## 容器

- 容器 = 執行的應用 & 執行環境
- 虛擬機器 = 執行的應用 & 模擬環境 (一整套作業系統，提供了執行態環境和其他系統環境)

### 啟動容器
```shell
$ docker run debian:latest /bin/echo 'hello world'
hello world
```
- 執行 echo hello world，後終止容器

```shell
$ docker run -t -i debian:latest /bin/bash
root@a999b45c2534:/#
```
- `-t` 分配虛擬終端並綁定到容器的標準輸入上
- `-i` 讓容器的標準輸入保持打開

```shell
$ docker run --rm hello-world
```
- `--rm` Automatically remove the container when it exits

### 守護態執行 (Daemonized)
```shell
$ docker run -d debian:latest /bin/bash -c "while true; do echo hello world; sleep 1; done"
0d6dd07e12486c0b31ea0e1b5053b63d2f75aeeac779586027ef66a1886e7376
```
```shell
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS               NAMES
0d6dd07e1248        debian:latest       "/bin/bash -c 'while "   6 seconds ago       Up 5 seconds                            angry_payne
```
```shell
$ docker logs 0d6dd07e1248
hello world
hello world
hello world
...
```

### 終止容器
```shell
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS               NAMES
0d6dd07e1248        debian:latest       "/bin/bash -c 'while "   6 seconds ago       Up 5 seconds                            angry_payne
```
```shell
$ docker stop 0d6dd07e1248
```

### 重新啟動
```shell
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS                       PORTS               NAMES
0d6dd07e1248        debian:latest       "/bin/bash -c 'while "   5 minutes ago       Exited (137) 3 minutes ago                       angry_payne
```
```shell
$ docker start 0d6dd07e1248
```

### 進入容器
```shell
$ docker run -idt debian
$ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
fa3819af1f0c        debian              "/bin/bash"         31 seconds ago      Up 30 seconds                           compassionate_bartik
```
```shell
$ docker exec -it fa3819af1f0c bash
root@fa3819af1f0c:/# exit
```
```shell
$ docker attach fa3819af1f0c
root@fa3819af1f0c:/# exit
```
- 使用 attach 命令有時候並不方便。當多個窗口同時 attach 到同一個容器的時候，所有窗口都會同步顯示。當某個窗口因命令阻塞時,其他窗口也無法執行操作了。

### 匯出容器快照
```shell
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
fa3819af1f0c        debian              "/bin/bash"         2 hours ago         Up About an hour                        compassionate_bartik
$ docker export fa3819af1f0c > debian.tar
```

### 從容器快照中匯入映像檔
```shell
$ cat debian.tar | docker import - debian:mytest
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
debian              mytest              b2d03ac11799        10 seconds ago      123 MB
debian              latest              ddf73f48a05d        13 days ago         123 MB
```

### 匯出同時匯入
```shell
$ docker export fa3819af1f0c | docker import - debian:mytest
```

### 比較 `load` 與 `import`
命令 | 動作 | 說明
----|----|----
`load` | 匯入映像檔儲存檔案到本地映像檔庫 | 保存完整記錄，檔案體積也跟著變大
`import` | 匯入容器快照檔案到本地映像檔庫 | 丟棄所有的歷史記錄和原始資料訊息 (僅保存容器當時的快照狀態)

### 刪除容器
```shell
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS                       PORTS               NAMES
0d6dd07e1248        debian:latest       "/bin/bash -c 'while "   5 minutes ago       Exited (137) 3 minutes ago                       angry_payne
```
```shell
$ docker rm 0d6dd07e1248
```

## 倉庫

### Docker Hub
```shell
$ docker search debian
NAME                           DESCRIPTION                                     STARS     OFFICIAL   AUTOMATED
debian                         Debian is a Linux distribution that's comp...   1714      [OK]
neurodebian                    NeuroDebian provides neuroscience research...   28        [OK]
jesselang/debian-vagrant       Stock Debian Images made Vagrant-friendly ...   9                    [OK]
armbuild/debian                ARMHF port of debian                            8                    [OK]
```
- 映像檔名字、描述、星級、是否官方建立、是否自動建立。 官方的映像檔說明是官方專案組建立和維護的，automated 資源允許使用者驗證映像檔的來源和內容。

自動建立（Automated Builds）功能對於需要經常升級映像檔內程式來說，十分方便。 有時候，使用者建立了映像檔，安裝了某個軟體，如果軟體發布新版本則需要手動更新映像檔。而自動建立允許使用者透過 Docker Hub 指定跟蹤一個目標網站（目前支援 GitHub 或 BitBucket）上的專案，一旦專案發生新的提交，則自動執行建立。

### 私有倉庫

有時候使用 Docker Hub 這樣的公共倉庫可能不方便，使用者可以建立一個本地倉庫供私人使用。

(指令略)

## 資料管理
如何在 Docker 內部以及容器之間管理資料

### 資料卷

### 建立一個資料卷
```shell
$ docker run -it -v /mydir debian /bin/bash
root@e2c9cd9fee7e:/# ls /
bin   dev  home  lib64 	mnt    opt   root  sbin  sys  usr
boot  etc  lib 	 media 	mydir  proc  run   srv 	 tmp  var
```
- 建立一個資料卷並掛載到容器裡

#### 掛載一個主機目錄作為資料卷
```shell
$ docker run -it -v ~/workspace.docker/mydir:/mydir debian:latest bash
root@6bad5f805a94:/# echo "hello world" > /mydir/hello.txt
root@6bad5f805a94:/# exit
exit
$ cat ~/workspace.docker/mydir/hello.txt
hello world
```
- 指定掛載一個本地主機的目錄到容器中，目錄不存在 Docker 會自動為你建立它

```shell
$ docker run -it -v ~/workspace.docker/mydir:/mydir:ro debian:latest bash
root@d3dc873a8516:/# cat /mydir/hello.txt
hello world
root@d3dc873a8516:/# echo "this is a test" >> /mydir/hello.txt
bash: /mydir/hello.txt: Read-only file system
```
- 透過 `:ro` 將資料卷設定為唯讀

### 資料卷容器
資料卷容器，其實就是一個正常的容器，專門用來提供資料卷供其它容器掛載的。

```shell
$ docker run -d -v /data --name data debian bash :              # hold a data volume /data
```
```shell
$ docker run -it --volumes-from data --name node1 debian bash
root@8fd3f0040a65:/# echo "hello world" > /data/hello.txt       # create a file in /data
root@8fd3f0040a65:/# exit
```
```shell
$ docker run -it --volumes-from data --name node2 debian bash
root@8cea6a320e2d:/# cat /data/hello.txt                        # read a file in /data
hello world
root@8cea6a320e2d:/# exit
```
- node1, node2 共用 data 容器的資料卷

## 使用網路

### 存取外部容器

#### 隨機映射連接埠 `-P`
```shell
$ docker run -d -P training/webapp python app.py
```
```shell
$ docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                     NAMES
057eef7d8782        training/webapp     "python app.py"     7 seconds ago       Up 6 seconds        0.0.0.0:32769->5000/tcp   pedantic_tesla
```

#### 映射所有遠端位址 `-p hostPort:containerPort`
```shell
$ docker run -d -p 5000:5000 training/webapp python app.py
```
```shell
$ docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                    NAMES
36eeb4a79745        training/webapp     "python app.py"     19 seconds ago      Up 18 seconds       0.0.0.0:5000->5000/tcp   loving_pasteur
```

#### 映射到指定位址的指定連接埠 `-p ip:hostPort:containerPort`
```shell
$ docker run -d -p 127.0.0.1:5000:5000 training/webapp python app.py
```
```shell
~/workspace.docker$ docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                      NAMES
a09fc5f3e52a        training/webapp     "python app.py"     5 seconds ago       Up 3 seconds        127.0.0.1:5000->5000/tcp   ecstatic_lamport
```

#### 映射到指定位址的任意連接埠 `-p ip::containerPort`
```shell
$ docker run -d -p 127.0.0.1::5000 training/webapp python app.py
```
```shell
$ docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                       NAMES
1de70b0b181b        training/webapp     "python app.py"     27 seconds ago      Up 26 seconds       127.0.0.1:32768->5000/tcp   amazing_elion
```

#### 查看映射連接埠配置
```shell
$ docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                      NAMES
a09fc5f3e52a        training/webapp     "python app.py"     5 seconds ago       Up 3 seconds        127.0.0.1:5000->5000/tcp   ecstatic_lamport
```
```shell
$ docker port ecstatic_lamport
5000/tcp -> 127.0.0.1:5000
```

### 容器互連
```shell
$ docker run -d --name db training/postgres
$ docker run -d -P --name web --link db:db training/webapp python app.py
```
- `--link name:alias`，其中 name 是要連接的容器名稱，alias 是這個連接的別名。

```shell
$ docker exec -it web bash
root@cd4b7660ef9a:/opt/webapp# env
HOSTNAME=cd4b7660ef9a
DB_NAME=/web/db
DB_PORT=tcp://172.17.0.2:5432
... (skipped)
root@cd4b7660ef9a:/opt/webapp# cat /etc/hosts
127.0.0.1      	localhost
172.17.0.2     	db 0dc415ed5ad5
172.17.0.3     	cd4b7660ef9a
root@cd4b7660ef9a:/opt/webapp# ping db
PING db (172.17.0.2) 56(84) bytes of data.
64 bytes from db (172.17.0.2): icmp_seq=1 ttl=64 time=0.300 ms
64 bytes from db (172.17.0.2): icmp_seq=2 ttl=64 time=0.078 ms
^C
--- db ping statistics ---
2 packets transmitted, 2 received, 0% packet loss, time 1003ms
rtt min/avg/max/mdev = 0.078/0.189/0.300/0.111 ms
root@cd4b7660ef9a:/opt/webapp# exit
exit
```
```shell
$ docker exec -it db bash
# ping 172.16.0.3
PING 172.16.0.3 (172.16.0.3) 56(84) bytes of data.
64 bytes from 172.16.0.3: icmp_seq=1 ttl=37 time=0.196 ms
64 bytes from 172.16.0.3: icmp_seq=2 ttl=37 time=0.352 ms
^C
--- 172.16.0.3 ping statistics ---
2 packets transmitted, 2 received, 0% packet loss, time 1001ms
rtt min/avg/max/mdev = 0.196/0.274/0.352/0.078 ms
root@0dc415ed5ad5:/# exit
```
