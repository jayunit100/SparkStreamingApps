package sparkapps.ctakes

import java.io.File

import com.google.gson.Gson
import com.google.gson._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import scala.runtime.ScalaRunTime._
/**
 * Collect at least the specified number of tweets into json text files.
 */
object Driver {
  val props = (
    "twitter4j.oauth.consumerKey",
    "twitter4j.oauth.consumerSecret",
    "twitter4j.oauth.accessToken",
    "twitter4j.oauth.accessTokenSecret"
  )

  private var numTweetsCollected = 0L
  private var partNum = 0
  private var gson = new Gson()
  private var defaults = Array(
    "--outputDirectory","/tmp/OUTPUT_"+System.currentTimeMillis(),
    "--numtweets","1000",
    "--intervals","10", //seconds
    "--partitions","1",
  "twitter4j.oauth.consumerKey","BOOPaRQKA8Gu8GjkHn4OaJsBx0",
  "twitter4j.oauth.consumerSecret","XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx",
  "twitter4j.oauth.accessToken","312897818-a6vB0wI9HBWV3kmYY9J6ccBuQ2XvoNfVkfdt1rYp",
  "twitter4j.oauth.accessTokenSecret","6gBUDqg116aLHN9Yi3K5mVBCxNlZoLB7JzX25NY3DTG0A"
  )

  def main(args: Array[String]) {
    System.out.println("START")
    // Process program arguments and set properties
    if (args.length == 0) {
      System.err.println("Usage: " + this.getClass.getSimpleName +
        "<outputDirectory> <numTweetsToCollect> <intervalInSeconds> <partitionsEachInterval>")
      System.out.println("running w/ defaults" +defaults);
      //call main with the defaults.
      main(defaults)
      //no more execution.
      return;
    }


    System.out.println("ARRAY");
    /**
     * Here we declare an array of values which map to the ordered.
     * Each value (i.e. numTweetsToCollect) is a newly declared variable that is
     * destructured from the parseCommandLineWithTwitterCredentials(args) monad.
     */
    val Array(
     //alphabetical order returned by values.
     Utils.IntParam(intervalSecs),
     Utils.IntParam(numTweetsToCollect),
     outputDirectory,
     Utils.IntParam(partitionsEachInterval)) =
        Parser.parse(defaults)

    System.out.println("Params = seconds=" + intervalSecs + " tweets="+ numTweetsToCollect+", "+ outputDirectory + " partitions=" +partitionsEachInterval)

    Utils.checkpoint(
      { x =>
        val pass = System.getProperty(x.toString)!=null;
        pass
      },
      {
        x => System.err.println("Failure: " + x)
      },
      List(props._1,props._2,props._3,props._4)
    )

    val outputDir = new File(outputDirectory.toString)
    if (outputDir.exists()) {
      System.err.println("ERROR - %s already exists: delete or specify another directory".format(
        outputDirectory))
      System.exit(1)
    }

    outputDir.mkdirs()

    println("Initializing Streaming Spark Context...")
    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName+""+System.currentTimeMillis())
      .setMaster("local[2]")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(intervalSecs))


    val tweetStream = TwitterUtilsJ.createStream(
      ssc,
      Utils.getAuth,
      Seq("medical"),
      StorageLevel.MEMORY_ONLY)
        .map(gson.toJson(_))
        .filter(!_.contains("boundingBoxCoordinates"))//some kind of spark jira to fix this.

    var checks = 0;

    var numRDDsCollected = 1
    tweetStream.foreachRDD(rdd => {
      System.out.println("\n\n\n...............if...\n\n\n")
      System.out.println("\n\n\n...............save...count...... \n\n\n")
      val outputRDD = rdd.repartition(partitionsEachInterval)
      System.out.println("************* "+outputRDD.toDebugString)
      System.out.println(rdd.count());
      System.out.println("\n\n\n...............!!!!!done counting!!!!!!...\n\n\n")
      numTweetsCollected += rdd.count()
      if (numTweetsCollected > numTweetsToCollect) {
          System.out.println("\n\n\n **** Exiting.");
          ssc.stop()
          sc.stop();
          System.exit(0)
      }
    })
    var stream = tweetStream.map(x => System.out.println("processed :::::::::: " + CtakesTermAnalyzer.analyze(x)));
    stream.print();
    ssc.start()
    ssc.awaitTermination()

  }
}
