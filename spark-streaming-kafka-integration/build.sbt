name := "spark-streaming-kafka"

version := "1.0.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.0.1"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.0.1"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.0.1"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.10.0.0"
