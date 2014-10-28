package sparkapps.ctakes



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
class TwitterInputDStreamCTakes(
                             @transient ssc_ : StreamingContext,
                             twitterAuth: Option[Authorization],
                             filters: Seq[String],
                             storageLevel: StorageLevel,
                             slideSeconds: Int
                             ) extends ReceiverInputDStream[Status](ssc_) {

    override def slideDuration(): Duration = {
      System.out.println("returning duration seconds = " + slideSeconds );
      return Seconds(slideSeconds)
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

    def statusListener():StatusListener = {
      new StatusListener {
        def onStatus(status: Status) = {
            System.out.println("Tweet  : "+status.getText)
            store(status)
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
      logInfo("Wating 5 seconds to start to prevent abuse.")
      Thread.sleep(5000)
      stopped=false;
      val future =
        new Thread(
          new Runnable() {
            def run() = {
              try {
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

      future.start();
    }

    def onStop() {
      stopped=true;
      setTwitterStream(null)
    }

    private def setTwitterStream(newTwitterStream: TwitterStream) = synchronized {
      if (twitterStream != null) {
        twitterStream.shutdown()
      }
      twitterStream = newTwitterStream
    }
  }

