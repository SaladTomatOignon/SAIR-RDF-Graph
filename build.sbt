name := "SAIR-RDF-Graph"

version := "0.1"

scalaVersion := "2.12.13"

val jenaVersion = "3.17.0"
val javafakerVersion = "1.0.2"
val jacksonVersion = "2.12.2"
val avroVersion = "0.9.7"
val sparkVersion = "3.1.1"
val kafkaVersion = "2.7.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test

libraryDependencies += "org.apache.jena" % "jena-core" % jenaVersion
libraryDependencies += "com.github.javafaker" % "javafaker" % javafakerVersion
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
libraryDependencies += "com.twitter" %% "bijection-avro" % avroVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-avro" % sparkVersion
libraryDependencies += "org.apache.kafka" % "kafka-streams" % kafkaVersion