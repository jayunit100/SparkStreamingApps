package sparkapps.tweetstream

import java.nio.ByteBuffer
import java.util.Date
import java.util.concurrent.{Callable, FutureTask}

import org.apache.ctakes.core.fsm.token.BaseToken
import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.jcas.JCas
import org.uimafit.factory.JCasFactory
import org.uimafit.pipeline.SimplePipeline
import org.uimafit.util.JCasUtil
import twitter4j._
import twitter4j.auth.Authorization
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization

import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.Logging
import org.apache.spark.streaming.receiver.Receiver

/**
 * A mock DStream implementation.
 *
 * This stream is used for unit testing the
 *
 * tweet processor, which should take a RececiverInputDSTream[Status] as input,
 * and process tweets.
 */
case class MockInputDStreamCTakes(sec:Long)(@transient ssc_ : StreamingContext)
  extends ReceiverInputDStream[Status](ssc_)  {

  override def slideDuration(): Duration = {
    return Seconds(sec)
  }

  override def getReceiver(): Receiver[Status] = {
    new Receiver[Status](StorageLevel.DISK_ONLY) {

      def newStatus(i:Long):Status = {
        new Status() {override def getPlace: Place = ???
          override def isRetweet: Boolean = ???
          override def isFavorited: Boolean = ???
          override def getCreatedAt: Date = ???
          override def getUser: User = ???
          override def getContributors: Array[Long] = ???
          override def getRetweetedStatus: Status = ???
          override def getInReplyToScreenName: String = ???
          override def isTruncated: Boolean = ???
          override def getId: Long = i*System.currentTimeMillis();
          override def getCurrentUserRetweetId: Long = ???
          override def isPossiblySensitive: Boolean = ???
          override def getRetweetCount: Long = ???
          override def getGeoLocation: GeoLocation = ???
          override def getInReplyToUserId: Long = ???
          override def getSource: String = ???
          override def getText: String = "a tweet # " + i + " " + System.currentTimeMillis();
          override def getInReplyToStatusId: Long = ???
          override def isRetweetedByMe: Boolean = ???
          override def compareTo(o: Status): Int = ???
          override def getHashtagEntities: Array[HashtagEntity] = ???
          override def getURLEntities: Array[URLEntity] = ???
          override def getMediaEntities: Array[MediaEntity] = ???
          override def getUserMentionEntities: Array[UserMentionEntity] = ???
          override def getAccessLevel: Int = ???
          override def getRateLimitStatus: RateLimitStatus = ???
        };
      }
      @volatile var killed=false;
      override def onStart() = {
        new Thread(
          new Runnable() {
            override def run(): Unit = {
              while(!killed) {
                System.out.println("-")
                //how does this = Status, but compiles to String?
                store(newStatus(System.currentTimeMillis()))
                Thread.sleep(1000);
              }
            }
          }).start()
      }
      override def onStop() = {
        System.out.println("Stop requested.");
        killed=true;
      }
    }
  }
}

