package sparkapps.ctakes

import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.receiver.Receiver
import twitter4j._
import twitter4j.auth.{Authorization, OAuthAuthorization}
import twitter4j.conf.ConfigurationBuilder



  /* A stream of Twitter statuses, potentially filtered by one or more keywords.
  *
  * @constructor create a new Twitter stream using the supplied Twitter4J authentication credentials.
  * An optional set of string filters can be used to restrict the set of tweets. The Twitter API is
  * such that this may return a sampled subset of all tweets during each interval.
  *
  * If no Authorization object is provided, initializes OAuth authorization using the system
  * properties twitter4j.oauth.consumerKey, .consumerSecret, .accessToken and .accessTokenSecret.
  */
case class TwitterInputDStreamCTakes(
                             @transient ssc_ : StreamingContext,
                             twitterAuth: Option[Authorization],
                             filters: Seq[String],
                             slideSeconds: Int) extends ReceiverInputDStream[Status](ssc_) {

     override def slideDuration(): Duration = {
      System.out.println("returning duration seconds = " + slideSeconds );
      return Seconds(slideSeconds)
    }

    private def createOAuthAuthorization(): Authorization = {
      new OAuthAuthorization(new ConfigurationBuilder().build())
    }

    private val authorization = twitterAuth.getOrElse(createOAuthAuthorization())

    override def getReceiver(): Receiver[Status] = {
      new TwitterReceiver(authorization, filters, StorageLevel.MEMORY_AND_DISK)
    }
  }

  class TwitterReceiver(
                         twitterAuth: Authorization,
                         filters: Seq[String],
                         storageLevel: StorageLevel
                         ) extends Receiver[Status](storageLevel) with Logging {

    @volatile private var twitterStream: TwitterStream = _

    override def store(status:Status): Unit = {
        super.store(status)
    }
    var received = 0;
    def statusListener():StatusListener = {
      new StatusListener {
        def onStatus(status: Status) = {
          received=received+1;
              System.out.println("Tweet  # " + status.getText)
              store(status)
              received=received+1;
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
    
    
    /**
    *  This will need to be injected somehow 
    *  so that we can have a unit test for confirming
    *  that the processor works correctly. 
    */
    def thread():Thread = {
        return new Thread(
          new Runnable() {
            def run() = {
              try {
                System.out.println("Consumer k = " + System.getProperty("twitter4j.oauth.consumerKey"))
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
    }
    
    override def onStart()= {
      logInfo("Wating 5 seconds to start to prevent abuse.")
      Thread.sleep(5000)
      stopped=false;
      val future = thread()
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