# Spark Cluster Mnanger 的選擇

- [APACHE SPARK CLUSTER MANAGERS: YARN, MESOS, OR STANDALONE?](http://www.agildata.com/apache-spark-cluster-managers-yarn-mesos-or-standalone/)
- [Apache Spark探秘：三种分布式部署方式比较](http://dongxicheng.org/framework-on-yarn/apache-spark-comparing-three-deploying-ways/)
- [Spark的三種集群deploy模式對比](https://read01.com/DzOPN5.html)
- [关于 Mesos，你知道多少？](http://blog.dataman-inc.com/guan-yu-mesosni-zhi-dao-duo-shao/)

上週六參加[Shanghai Apache Spark Meetup第十一次聚会](http://huiyi.csdn.net/activity/closed?project_id=3320)，最後一個 session 是《Spark on Mesos的实践》，講者孙锐介紹 Mesos 資源管理原理，並分享Spark on Mesos实践的经验教训。

以下整理幾個 Spark Cluster Manager 的比較。

## Scheduling

Standalone:
- a simple FIFO scheduler
- 預設 application 可使用集群中所有可用節點
- application 可透過 SparkConf 限制 cpu, mem 數量

Spark on Yarn:
- Scheduler + ApplicationsManager
- Scheduler：把 application 放到 queue，等候資源分配。
- ApplicationsManager：負責接收 job，啟動 ApplicationsMaster。
  - ApplicationsMaster = Spark application
  - resources 定義在 SparkConf

Spark on Mesos:
- master & slave
- master 提供資源給 application (在 Spark on Mesos 中稱作 framework)，application 請求資源，如果接受 master 提供的 offer 就會執行 jobs
  - fine-grained control mode: 控制 cpus, memory, disks, ports 的分配
  - course-grained control mode: 事先分配固定數量的 cpu 給給個 executor，直到 application 結束才會釋放資源

## High Availability

## Security

## Monitoring
