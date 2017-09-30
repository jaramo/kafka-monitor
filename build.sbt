name := "Kafka Monitor"

version := "0.0.1"

scalaVersion := "2.12.3" // "2.11.11"

lazy val kafkaVersion = "0.10.2.1"

logBuffered in Test := false
parallelExecution in Test := false

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(

  "org.apache.kafka" %% "kafka" % kafkaVersion exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.scalaz" %% "scalaz-core" % "7.2+",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7+",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.pureconfig" %% "pureconfig" % "0.8.0",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.10.0" % "test"
)