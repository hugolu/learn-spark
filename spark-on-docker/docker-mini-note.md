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
倉庫 (Repository) | 集中存放映像檔檔案的場所 | 最大公開倉庫 [Docker Hub](https://hub.docker.com/), 中國公開倉庫 [时速云](https://hub.tenxcloud.com/), [网易蜂巢](https://c.163.com/hub)

> 映像檔是唯讀的，容器在啟動的時候建立一層可寫層作為最上層
