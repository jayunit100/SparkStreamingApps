name := "SparkSBT"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.1.0"

libraryDependencies +=  "org.scalatest" % "scalatest_2.10.0-M4" % "1.9-2.10.0-M4-B1"

libraryDependencies +=  "junit" % "junit" % "4.8.1" % "test"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-twitter" % "1.1.0"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "3.0.3"

libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"
