package sparkapps.ctakes

import java.io.File

import com.datastax.spark.connector.cql.CassandraConnector
import com.google.gson.Gson
import com.google.gson._
import jregex.Pattern
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import sparkapps.ctakes.TwitterInputDStreamCTakes
import sparkapps.tweetstream._
import twitter4j.Status
import twitter4j.auth.{OAuthSupport, Authorization}
import scala.runtime.ScalaRunTime._
/**
 * Collect at least the specified number of tweets into json text files.
 */
object Driver {

  private var numTweetsCollected = 0L
  private var partNum = 0
  private var gson = new Gson()
var master : String = "";
  /**
   * Maintain twitter credentials in a /tmp/twitter file.
   * @param twitterParam
   * @return
   */
  def readParameter(twitterParam:String) : Option[String] = {
    if(! new File("/tmp/twitter").exists()){
      System.err.println("MAJOR failure.  No /tmp/twitter file exists.")
      None;
    }
    //find the parameter in /tmp/twitter.
    def read(param:String):Option[String]= {
      scala.io.Source.fromFile("/tmp/twitter").getLines().foreach {
        x =>
          System.out.println("line : " + x)
          if (! x.contains("=")){
            System.err.println("MAJOR failure.  Bad line " +x + " in /tmp/twitter.")
          }
          else if (x.contains(param)) {
            return Some(x.split("=")(1));
          }
      }
      System.err.println("Uhoh ! Didn't see the twitter param in /tmp/twitter for " + twitterParam );
      None
    }

    //just return it for now , in future maybe we'll handle errors by prompting user.
    val x = read(twitterParam);
    x match {
      case Some(x) => return Some(x);
      case None => {
       //FUTURE : Prompt user for this parameter.
       return None;
      }
    }
  }
  /**
   * Input= --outputDirectory --numtweets --intervals --partitions
   * Output= outputdir numtweets  intervals partitions consumerKey consumerSecret accessToken accessTokenSecret
   */
  def main(args: Array[String]) {
    def failTwFile() = {
      System.err.println("FAILURE to read values from /tmp/twitter credentials. ")
      System.err.println("Please write a k/v file like this:")
      System.err.println("consumerKey=xxx")
      System.err.println("consumerSecret=yyy")
      System.err.println("accessToken=zzz")
      System.err.println("accessTokenSecret=aaa")
      System.err.println("To /tmp/twitter, and restart this app.")
      System.exit(2)
    }

    System.out.println("START:  Put consumerkey,consumer_secret,access_token,access_token_secret in /tmp/twitter, " +
      "or it will be written for you interactively....")
    if(args.length==0) {
      val defs = Array(
        "--outputDirectory", "/tmp/tweets", //only used for the file endpoint impl.
        "--numtweets", "10",
        "--intervals", "60", /// this is what determines intervals......
        "--partitions", "1",
        "--master", "local[2]",
        //added as system properties.
        /** qoute at the end is for type inference **/
        "twitter4j.oauth." + Parser.CONSUMER_KEY, readParameter(Parser.CONSUMER_KEY).getOrElse({failTwFile(); ""}),
        "twitter4j.oauth." + Parser.CONSUMER_SECRET, readParameter(Parser.CONSUMER_SECRET).getOrElse({failTwFile(); ""}),
        "twitter4j.oauth." + Parser.ACCESS_TOKEN, readParameter(Parser.ACCESS_TOKEN).getOrElse({failTwFile() ; ""}),
        "twitter4j.oauth." + Parser.ACCESS_TOKEN_SECRET, readParameter(Parser.ACCESS_TOKEN_SECRET).getOrElse({failTwFile(); ""})
      );

      //TODO clean up this.  Could lead to infinite recursion if minor coding error happens in this function.
      System.err.println("Usage: " + this.getClass.getSimpleName + " executing w/ default options ! " + defs)
      main(defs);
      return;
    }
     /**
     * Here we declare an array of values which map to the ordered.
     * Each value (i.e. numTweetsToCollect) is a newly declared variable that is
     * destructured from the parseCommandLineWithTwitterCredentials(args) monad.
     */
    val Array(
    //alphabetical order returned by values.
    Utils.IntParam(intervalSecs),
    master,
    Utils.IntParam(numTweetsToCollect),
    outputDirectory,
    Utils.IntParam(partitionsEachInterval)
    ) = Parser.parse(args)

    System.out.println("interval seconds " + intervalSecs );
    verifyAndRun(intervalSecs,numTweetsToCollect, new File(outputDirectory), partitionsEachInterval);
  }

  def verify() = {
    /**
     * Checkpoint confirms that each system property exists.
     */
    Utils.checkpoint(
    //verifier
    {
      xp =>
        System.getProperty(xp.toString) != null;
    },
    //error messages.
    {
      xp => System.err.println("Failure: " + xp)
    },
    //properties to be verified.
    List(
      "twitter4j.oauth.consumerKey",
      "twitter4j.oauth.consumerSecret",
      "twitter4j.oauth.accessToken",
      "twitter4j.oauth.accessTokenSecret"
     )
    )
  }

  def verifyAndRun(intervalSecs:Int, numTweetsToCollect:Int, outputDirectory:File, partitionsEachInterval:Int) = {

    System.out.println(
      "Params = seconds= " + intervalSecs +
        " tweets= " + numTweetsToCollect + ", " +
        " out =" + outputDirectory + ", " +
        " partitions= " + partitionsEachInterval)

    verify();

    if (outputDirectory.exists()) {
      System.err.println("ERROR - %s already exists: delete or specify another directory".format(outputDirectory))
      System.exit(2)
    }
    runDISK("local[2]", intervalSecs, partitionsEachInterval, numTweetsToCollect, outputDirectory);

    //runCassandra(....)
  }

  def streamingFunction(sssc: StreamingContext): ReceiverInputDStream[Status] = {
    TwitterUtils.createStream(
          sssc,
          Utils.getAuth,
          Seq(
            "Abilify,Nexium,Humira,Crestor,Advair Diskus,Enbrel,"+
              "Remicade,Cymbalta,Copaxone,Neulasta,Lantus Solostar,"+
              "Rituxan,Spiriva Handihaler,Januvia,Atripla,Lantus,Oxycontin,"+
              "Celebrex,Celebrex,Diovan,Gleevec,Herceptin,Lucentis,Namenda,"+
              "Truvada,Enbrel,Ranexa,Humalog,Novolog,Tamiflu,Januvia,Namenda,"+
              "Benicar,Nasonex,Suboxone,Symbicort,Bystolic,Oxycontin,Xarelto"),
          StorageLevel.MEMORY_AND_DISK)
  }

  def runDISK(master:String, intervalSecs:Int, partitionsEachInterval:Int, numTweetsToCollect:Int, file:File) = {
    println("Initializing Streaming Spark Context...")

    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName + "" + System.currentTimeMillis())
      .setMaster(master)
    val sCon = new SparkContext(conf)
    val ssc = new StreamingContext(sCon, Seconds(10));
    val tweetStream: ReceiverInputDStream[Status] = streamingFunction(ssc);

    //lots of empty files if 10 second interval, obviously.
    tweetStream.saveAsTextFiles(file.getAbsolutePath)
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
    System.exit(0)
  }

  /**
   * Example of cassandra implementation.
   * Not yet supported by the app but easy to add
   * by simply updating the parameters for setting up the cassandra connector etc.
   */
  def runCassandra(master:String, intervalSecs:Int, partitionsEachInterval:Int, numTweetsToCollect:Int) = {
    println("Initializing Streaming Spark Context...")

    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName + "" + System.currentTimeMillis())
      .setMaster(master)
    val sCon = new SparkContext(conf)
    val ssc = new StreamingContext(sCon, Seconds(10));
    val tweetStream: ReceiverInputDStream[Status] = streamingFunction(ssc);

    tweetStream.foreachRDD( transactions => {
        CassandraConnector(conf).withSessionDo {
        session => {
          val x=1
          Thread.sleep(1)
          transactions.foreach({
            xN =>
              System.out.println("Running Cassandra Insert..." + xN)
              System.out.println("Note that this can fail if cassandra isnt working...")
              val xNtxt=xN.toString+" "+xN.getText;
              session.executeAsync(s"INSERT INTO streaming_test.key_value (key, value) VALUES ('$xNtxt' , $x)")
          })
        }
      }
    })
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
    System.exit(0)

  }

}