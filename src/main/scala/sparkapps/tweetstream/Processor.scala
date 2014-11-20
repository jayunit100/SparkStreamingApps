package sparkapps.tweetstream

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkContext, SparkConf}
import sparkapps.ctakes.{CtakesTermAnalyzer, Utils, TwitterUtilsCtakes}
import com.google.gson.Gson
import com.google.gson._
import jregex.Pattern
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.cql._

/**
 * Completely application neutral stream processor.
 */
object Processor {

  import org.apache.spark.rdd;
  val total=10;
  var gson = new Gson()

  /**
   * FYI This should fail fast for you if cassandra isnt set up right :).
   * make sure and turn off iptables if you get a "operation timed out" exception.
   */
  def startCassandraStream() = {
      val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName + "" + System.currentTimeMillis())
      .setMaster("local[2]")
      .set("spark.cassandra.connection.host", "127.0.0.1")
       .set("spark.cassandra.connection.native.port","9042")
      .set("spark.cassandra.auth.username", "cassandra")
      .set("spark.cassandra.auth.password", "cassandra")

      CassandraConnector(conf).withSessionDo { session =>
        System.out.println("Cassandra 1")
        // cqlsh --e "CREATE KEYSPACE IF NOT EXISTS streaming_test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 3 }"
        session.execute(s"CREATE KEYSPACE IF NOT EXISTS streaming_test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 3 }")
        System.out.println("Cassandra 2")
        session.execute(s"CREATE TABLE IF NOT EXISTS streaming_test.key_value (key VARCHAR PRIMARY KEY, value INT)")
        System.out.println("Cassandra 3")
        session.execute(s"TRUNCATE streaming_test.key_value")
      }
      //now start the stream.
      startStream(conf, 1, 1, 1)
  }

  def startStream(sparkConf:SparkConf, intervalSecs:Int, partitionsEachInterval:Int, numTweetsToCollect:Int) = {
    println("Initializing Streaming Spark Context...")

    println("Initializing Streaming Spark Context... 2")

    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(intervalSecs))
    println("Initializing Streaming Spark Context... 3")

    val stream  =
      new MockInputDStreamCTakes(total,ssc)
        .map(gson.toJson(_))
        .filter(!_.
          contains("boundingBoxCoordinates"));

    var count = 0;
    println("Initializing Streaming Spark Context... 4")

    //without this, nothing will execute.
    stream.foreachRDD(rdd => {
      System.out.println("fir each " + count);
      count+=1

      if (count>2) {
        //assumes session.
        CassandraConnector(sparkConf).withSessionDo
        { session =>
          val xN="n"+System.currentTimeMillis()+count
          val x=1
          Thread.sleep(1)
          System.out.println("Cassandra 5")
          session.execute(s"INSERT INTO streaming_test.key_value (key, value) VALUES ('$xN' , $x)")
          System.out.println("Cassandra 6")
        }

        System.out.println("done ; killing.")
        ssc.stop()
        sc.stop();
        System.exit(0)
      }
    })

    //stream.print();
    ssc.start()
    ssc.awaitTermination()}

}