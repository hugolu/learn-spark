# Docker 簡易筆記

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

## Docker Image

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
