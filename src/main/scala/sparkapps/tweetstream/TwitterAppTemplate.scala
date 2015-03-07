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
  var gson = new Gson()

  def startStream2(slideSeconds:Int, sparkConf: SparkConf,
                  stream: (StreamingContext => ReceiverInputDStream[Status]),
                  fileNamePref: String) = {
    val sc = new SparkContext(sparkConf);

    val ssc = new StreamingContext(sc, Seconds(slideSeconds))

    var count = 0;
    println("Initializing Streaming Spark Context...  slider window "+slideSeconds)

    //Without this, nothing will execute: Streaming context's require an attached consumer to run.
    TwitterUtils.createStream(
      slideSeconds,
      ssc,
      Utils.getAuth,
      Seq("Abilify,Nexium,Humira,Crestor,Advair Diskus,Enbrel,"+
        "Remicade,Cymbalta,Copaxone,Neulasta,Lantus Solostar,"+
        "Rituxan,Spiriva Handihaler,Januvia,Atripla,Lantus,Oxycontin,"+
        "Celebrex,Celebrex,Diovan,Gleevec,Herceptin,Lucentis,Namenda,"+
        "Truvada,Enbrel,Ranexa,Humalog,Novolog,Tamiflu,Januvia,Namenda,"+
        "Benicar,Nasonex,Suboxone,Symbicort,Bystolic,Oxycontin,Xarelto"),

    StorageLevel.MEMORY_AND_DISK).saveAsTextFiles(fileNamePref)

    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
    sc.stop()
    System.exit(0)

  }

  def startStream(slideSeconds:Int, sparkConf: SparkConf,
                  stream: (StreamingContext => ReceiverInputDStream[Status]),
                  pluggableETLFunction: (Array[Status], SparkConf) => Boolean) = {
    val sc = new SparkContext(sparkConf);

    val ssc = new StreamingContext(sc, Seconds(slideSeconds))

    var count = 0;
    println("Initializing Streaming Spark Context... 4")

    //Without this, nothing will execute: Streaming context's require an attached consumer to run.
    TwitterUtils.createStream(
    slideSeconds,
      ssc,
      Utils.getAuth,
      Seq("medical"),
      StorageLevel.MEMORY_AND_DISK)
      .foreachRDD(rdd => {
          pluggableETLFunction(rdd.collect(), sparkConf)

      })

    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
    sc.stop()
    System.exit(0)
  }





}