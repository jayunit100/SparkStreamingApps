package sparkapps

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
object Collect {
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
    "--numtweets","10",
    "--intervals","10", //seconds
    "--partitions","1",
  
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
      .setMaster("local")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(intervalSecs))


    val tweetStream = TwitterUtilsJ.createStream(
      ssc,
      Utils.getAuth,
      Seq("medical"),
      StorageLevel.MEMORY_ONLY_2)
      .map(gson.toJson(_))
      .filter(!_.contains("boundingBoxCoordinates"))//some kind of spark jira to fix this.

    var checks = 0;

    tweetStream.foreachRDD((rdd,lent)=> {
//      tweetStream.repartition(1)
//      tweetStream.foreachRDD((rdd, time) => {
       numTweetsCollected+=1;
      val count = rdd.count()
      System.out.println("RDDS = " + count)

      //numTweetsCollected+=rdd.collect().length;
      checks += 1;
      if (checks > 20) {
        System.out.println("STOPPING CONTEXT !!!!!!!!!!!! ")
        ssc.stop()
      }
      //tweetStream.context.stop();
        System.out.println("COLLECTED " + numTweetsCollected);
     //   System.out.println(stringOf("RDD :::: " + rdd.collect()));
        if (numTweetsCollected > numTweetsToCollect) {
          System.out.println("EXIT 0 ")
           System.exit(0)
        }

        if (numTweetsCollected > numTweetsToCollect) {
          System.out.println("done collecting all " + numTweetsCollected + " tweets");
          tweetStream.context.stop();
        }
    })

    ssc.start()
    ssc.awaitTermination()

  }
}
