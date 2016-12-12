# Spark Cluster Mnanger 的選擇

- [APACHE SPARK CLUSTER MANAGERS: YARN, MESOS, OR STANDALONE?](http://www.agildata.com/apache-spark-cluster-managers-yarn-mesos-or-standalone/)
- [Apache Spark探秘：三种分布式部署方式比较](http://dongxicheng.org/framework-on-yarn/apache-spark-comparing-three-deploying-ways/)
- [Spark的三種集群deploy模式對比](https://read01.com/DzOPN5.html)
- [关于 Mesos，你知道多少？](http://blog.dataman-inc.com/guan-yu-mesosni-zhi-dao-duo-shao/)

上週六參加[Shanghai Apache Spark Meetup第十一次聚会](http://huiyi.csdn.net/activity/closed?project_id=3320)，最後一個 session 是《Spark on Mesos的实践》，講者孙锐介紹 Mesos 資源管理原理，並分享Spark on Mesos实践的经验教训。

以下整理幾個 Spark Cluster Manager 的比較。

- Spark Standalone: 作為Spark的一部分,Standalone是一個簡單的集群管理器。它具有master的HA，彈性應對WorkerFailures，對每個應用程式的管理資源的能力，並且可以在現有的Hadoop一起運行和訪問HDFS的數據。該發行版包括一些腳本，可以很容易地部署在本地或在AmazonEC2雲計算。它可以在Linux，Windows或Mac OSX上運行。
- Apache Mesos: 分布式系統內核，具有HA的masters和slaves，可以管理每個應用程式的資源，並對Docker容器有很好的支持。它可以運行Spark工作， Hadoop的MapReduce的，或任何其他服務的應用程式。它有Java， Python和C ++ 的API。它可以在Linux或Mac OSX上運行。
- Hadoop YARN: 作業調度和集群資源管理的分布式計算框架，具有HA為masters和slaves，在非安全模式下支持Docker容器，在安全模式下支持Linux和Windows container executors，和可插拔的調度器。它可以運行在Linux和Windows上運行。

## Scheduling

Standalone:
- a simple FIFO scheduler
- 預設 application 可使用集群中所有可用節點
- application 可透過 SparkConf 限制 cpu, mem 數量

Spark on Mesos:
- master & slave
- master 提供資源給 application (在 Spark on Mesos 中稱作 framework)，application 請求資源，如果接受 master 提供的 offer 就會執行 jobs
  - fine-grained control mode: 控制 cpus, memory, disks, ports 的分配
  - course-grained control mode: 事先分配固定數量的 cpu 給給個 executor，直到 application 結束才會釋放資源

Spark on Yarn:
- Scheduler + ApplicationsManager
- Scheduler：把 application 放到 queue，等候資源分配。
- ApplicationsManager：負責接收 job，啟動 ApplicationsMaster。
  - ApplicationsMaster = Spark application
  - resources 定義在 SparkConf

## High Availability

Standalone:
- automatic recovery of the master by using standby masters in a ZooKeeper quorum.
- manual recovery using the file system.

Spark on Mesos:
- automatic recovery of the master using Apache ZooKeeper
- Tasks which are currently executing continue to do so in the case of failover.

Spark on Yarn:
- manual recovery using a command line utility 
- automatic recovery via a Zookeeper-based ActiveStandbyElector embedded in the ResourceManager

## Security

Standalone:
The standalone manager requires the user configure each of the nodes with the shared secret. 
- Data can be encrypted using SSL for the communication protocols.
- Access to Spark applications in the Web UI can be controlled via access control lists.

Spark on Mesos:
- Mesos’ default authentication module, Cyrus SASL, can be replaced with a custom module. 
- Access control lists are used to authorize access to services in Mesos.
- By default, communication between the modules in Mesos is unencrypted. SSL/TLS can be enabled to encrypt this communication. HTTPS is supported for the Mesos WebUI.

Spark on Yarn:
- Hadoop authentication uses Kerberos to verify that each user and service is authenticated by Kerberos.
- Access to the Hadoop services can be finely controlled via access control lists.
- data and communication between clients and services can be encrypted using SSL and data transferred between the Web console and clients with HTTPS

## Monitoring

Standalone:
- Spark’s standalone cluster manager has a Web UI to view cluster and job statistics as well as detailed log output for each job.

Spark on Mesos:
- Apache Mesos provides numerous metrics for the master and slave nodes accessible via a URL.
  - percentage and number of allocated cpu’s, total memory used, percentage of available memory used, total disk space, allocated disk space, elected master, uptime of a master, slave registrations, connected slaves...

Spark on Yarn:
- Hadoop YARN has a Web UI for the ResourceManager and the NodeManager.
- The ResourceManager UI provides metrics for the cluster
- the NodeManager provides information for each node and the applications and containers running on the node.
