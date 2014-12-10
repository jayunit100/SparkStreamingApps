package sparkapps.tweetstream

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkContext, SparkConf}
import sparkapps.ctakes.CtakesTermAnalyzer
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
import twitter4j.Status

/**
 * Completely application neutral stream processor
 * for twitter data.
 * TODO.  This should load 3 implicits.
 * 1) total tweets to process.
 * 2) function for dealing with Status objects
 * 3) InputDStream[Status] implementation.
 *
 * Unit tests will use mock input stream.
 * Meanwhile, the production app will create implicits for real data
 * which gets put into hbase.
 */
object TwitterAppTemplate {

  import org.apache.spark.rdd;
  val total=10;
  var gson = new Gson()
  var intervalSecs = 5;

  def startStream(sparkConf:SparkConf,
                  stream:(StreamingContext => ReceiverInputDStream[Status]),
                  pluggableETLFunction:(Array[Status],SparkConf)=>Boolean) = {
    val sc=new SparkContext(sparkConf);

    val ssc=new StreamingContext(sc, Seconds(intervalSecs))

    var count = 0;
    println("Initializing Streaming Spark Context... 4")


    //Without this, nothing will execute: Streaming context's require an attached consumer to run.
    TwitterUtils.createStream(
      ssc,
      Utils.getAuth,
      Seq("medical"),
      StorageLevel.MEMORY_AND_DISK)
        .foreachRDD(rdd => {
          count+=1
          if (count>2) {
            pluggableETLFunction(rdd.collect(),
            ///<<<<<<<<<< TODO ::: is this still NULL
              sparkConf)
            ssc.stop()
            sc.stop();
            System.exit(0)
          }
    })

    ssc.start()
    ssc.awaitTermination()
  }
}