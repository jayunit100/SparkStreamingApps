package sparkapps.tweetstream

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.api.java.{JavaReceiverInputDStream, JavaStreamingContext}
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import sparkapps.ctakes.TwitterInputDStreamCTakes
import twitter4j.Status
import twitter4j.auth.Authorization

/**
 * Moddified and borrowed from databricks spark tutorial.
 */
object TwitterUtils {
  /**
   * Create a input stream that returns tweets received from Twitter.
   * @param ssc         StreamingContext object
   * @param twitterAuth Twitter4J authentication, or None to use Twitter4J's default OAuth
   *        authorization; this uses the system properties twitter4j.oauth.consumerKey,
   *        twitter4j.oauth.consumerSecret, twitter4j.oauth.accessToken and
   *        twitter4j.oauth.accessTokenSecret
   * @param filters Set of filter strings to get only those tweets that match them
   * @param storageLevel Storage level to use for storing the received objects
   */
  def createStream( slideSeconds:Int,
                    ssc: StreamingContext,
                    twitterAuth: Option[Authorization],
                    filters: Seq[String] = Nil,
                    storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
                    ): ReceiverInputDStream[Status] =
  {
    //2 second slide duratoin
    System.out.println("slideSeconds " + slideSeconds);
    new TwitterInputDStreamCTakes( ssc, twitterAuth, filters, slideSeconds)

  }
  //other implementations deleted, grab them from the upstream code if needed.
}
