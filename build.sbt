name := "SparkSBT"

version := "1.0"

scalaVersion := "2.10.4"

net.virtualvoid.sbt.graph.Plugin.graphSettings

//packSettings


mergeStrategy in assembly := { 
      case n if n.startsWith("META-INF/eclipse.inf") => MergeStrategy.discard
        case n if n.startsWith("META-INF/ECLIPSEF.RSA") => MergeStrategy.discard
          case n if n.startsWith("META-INF/ECLIPSE_.RSA") => MergeStrategy.discard
            case n if n.startsWith("META-INF/ECLIPSEF.SF") => MergeStrategy.discard
              case n if n.startsWith("META-INF/ECLIPSE_.SF") => MergeStrategy.discard
                case n if n.startsWith("META-INF/MANIFEST.MF") => MergeStrategy.discard
                  case n if n.startsWith("META-INF/NOTICE.txt") => MergeStrategy.discard
                    case n if n.startsWith("META-INF/NOTICE") => MergeStrategy.discard
                      case n if n.startsWith("META-INF/LICENSE.txt") => MergeStrategy.discard
                        case n if n.startsWith("META-INF/LICENSE") => MergeStrategy.discard
                          case n if n.startsWith("rootdoc.txt") => MergeStrategy.discard
                            case n if n.startsWith("readme.html") => MergeStrategy.discard
                              case n if n.startsWith("readme.txt") => MergeStrategy.discard
                                case n if n.startsWith("library.properties") => MergeStrategy.discard
                                  case n if n.startsWith("license.html") => MergeStrategy.discard
                                    case n if n.startsWith("about.html") => MergeStrategy.discard
                                      case _ => MergeStrategy.last
}

libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "1.1.0-beta1" withSources() withJavadoc()

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.1.0"

// #libraryDependencies += "org.apache.spark" %% "spark-core" % "1.1.0"

libraryDependencies +=  "org.scalatest" % "scalatest_2.10.0-M4" % "1.9-2.10.0-M4-B1"

libraryDependencies +=  "junit" % "junit" % "4.8.1" % "test"

//libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.1.0"

//libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-twitter" % "1.1.0"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "3.0.3"

libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

libraryDependencies += "org.apache.ctakes" % "ctakes-core" % "3.2.1"

libraryDependencies += "org.apache.ctakes" % "ctakes-core-res" % "3.2.1"

libraryDependencies += "org.apache.ctakes" % "ctakes-constituency-parser" % "3.2.1"

libraryDependencies += "org.apache.ctakes" % "ctakes-clinical-pipeline" % "3.2.1"

libraryDependencies += "org.apache.ctakes" % "ctakes-dictionary-lookup-fast" % "3.2.1"

libraryDependencies += "org.apache.ctakes" % "ctakes-drug-ner" % "3.2.1"

libraryDependencies += "org.apache.ctakes" % "ctakes-assertion" % "3.2.1"


javaOptions += "-Xmx2G"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

resolvers += "opennlp sourceforge repo" at "http://opennlp.sourceforge.net/maven2"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/releases/"
