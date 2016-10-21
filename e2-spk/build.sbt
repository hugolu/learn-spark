name := "e2-spk-app"

version := "1.0.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.0.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.0.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-8_2.11" % "2.0.0"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.10.0.0"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.0.0"

// dependencies for java
libraryDependencies += "io.socket" % "socket.io-client" % "0.7.0"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.1"
libraryDependencies += "commons-cli" % "commons-cli" % "1.3.1"
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.21"
libraryDependencies += "io.dropwizard.metrics" % "metrics-json" % "3.1.2"
