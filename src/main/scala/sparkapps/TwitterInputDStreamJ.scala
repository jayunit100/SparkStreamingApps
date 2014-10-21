package sparkapps

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


  /* A stream of Twitter statuses, potentially filtered by one or more keywords.
  *
  * @constructor create a new Twitter stream using the supplied Twitter4J authentication credentials.
  * An optional set of string filters can be used to restrict the set of tweets. The Twitter API is
  * such that this may return a sampled subset of all tweets during each interval.
  *
  * If no Authorization object is provided, initializes OAuth authorization using the system
  * properties twitter4j.oauth.consumerKey, .consumerSecret, .accessToken and .accessTokenSecret.
  */
class TwitterInputDStreamJ(
                             @transient ssc_ : StreamingContext,
                             twitterAuth: Option[Authorization],
                             filters: Seq[String],
                             storageLevel: StorageLevel
                             ) extends ReceiverInputDStream[Status](ssc_) {

    override def slideDuration(): Duration = {
      System.out.println("checking duration");
      return Seconds(10)
    }

    private def createOAuthAuthorization(): Authorization = {
      new OAuthAuthorization(new ConfigurationBuilder().build())
    }

    private val authorization = twitterAuth.getOrElse(createOAuthAuthorization())

    override def getReceiver(): Receiver[Status] = {
      new TwitterReceiver(authorization, filters, storageLevel)
    }
  }

  class TwitterReceiver(
                         twitterAuth: Authorization,
                         filters: Seq[String],
                         storageLevel: StorageLevel
                         ) extends Receiver[Status](storageLevel) with Logging {

    @volatile private var twitterStream: TwitterStream = _
    var total=0;
    override def store(status:Status): Unit = {
        super.store(status)
    }

    def mockStatus: Status = {
      new Status {override def getPlace: Place = ???

        override def isRetweet: Boolean = ???

        override def isFavorited: Boolean = ???

        override def getCreatedAt: Date = ???

        override def getUser: User = ???

        override def getContributors: Array[Long] = ???

        override def getRetweetedStatus: Status = ???

        override def getInReplyToScreenName: String = "------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"

        override def isTruncated: Boolean = ???

        override def getId: Long = ???

        override def getCurrentUserRetweetId: Long = ???

        override def isPossiblySensitive: Boolean = ???

        override def getRetweetCount: Long = ???

        override def getGeoLocation: GeoLocation = ???

        override def getInReplyToUserId: Long = ???

        override def getSource: String = System.currentTimeMillis()+"SADFSADFASDFSDFSDFFffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"

        override def getText: String = "ASDFsdofaidsjofaisjdofiajsdofijadsfASDFsdofaidsjofaisjdofiajsdofijadsfASDFsdofaidsjofaisjdofiajsdofijadsfASDFsdofaidsjofaisjdofiajsdofijadsf"+System.currentTimeMillis()

        override def getInReplyToStatusId: Long = ???

        override def isRetweetedByMe: Boolean = ???

        override def compareTo(p1: Status): Int = ???

        override def getHashtagEntities: Array[HashtagEntity] = ???

        override def getURLEntities: Array[URLEntity] = ???

        override def getMediaEntities: Array[MediaEntity] = ???

        override def getUserMentionEntities: Array[UserMentionEntity] = ???

        override def getAccessLevel: Int = ???

        override def getRateLimitStatus: RateLimitStatus = ???

      }
    }
    def statusListener():StatusListener = {
      new StatusListener {
        def onStatus(status: Status) = {
          System.out.println("Storing 10000 statuses... " + status.getText);
          for (x <- 1 to 10000) {
            store(status)
          }
        }
        def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

        def onTrackLimitationNotice(i: Int) {}

        def onScrubGeo(l: Long, l1: Long) {}

        def onStallWarning(stallWarning: StallWarning) {}

        def onException(e: Exception) {
          e.printStackTrace();
          Thread.sleep(10000);
          if(! stopped) {
            restart("Error receiving tweets", e)
          }
        }
      }
    }
    @volatile var stopped = false;
    override def onStart()= {
      System.out.println("*************STARTING*******************")

      stopped=false;
      val future =
        new Thread(
          new Runnable() {
            def run() = {
              try {
                /**
               for(x <- 1 until 1000) {
                 if(x%100==0){
                   System.out.println("status " + x);
                   //stop("killing..");
                 }
                 store(mockStatus);
               }
                  **/

                val newTwitterStream = new TwitterStreamFactory().getInstance(twitterAuth)
                newTwitterStream.addListener(statusListener)

                val query = new FilterQuery
                if (filters.size > 0) {
                  query.track(filters.toArray)
                  newTwitterStream.filter(query)
                }
                else {
                  newTwitterStream.sample()
                }
                setTwitterStream(newTwitterStream)

                logInfo("Twitter receiver started")
              }
              catch {
                case e: Exception =>
                  restart("Error starting Twitter stream", e)
              }
            }
          });

      System.out.println("STARTING.................")
      future.start();
      System.out.println("FUTURE RETURNING.................")
    }

    def onStop() {
      stopped=true;
      setTwitterStream(null)
      logInfo("Twitter receiver stopped")
      logInfo("Wating 10 seconds, to prevent abuse.")
      Thread.sleep(10000)
    }

    private def setTwitterStream(newTwitterStream: TwitterStream) = synchronized {
      if (twitterStream != null) {
        twitterStream.shutdown()
      }
      twitterStream = newTwitterStream
    }
  }

