package sparkapps.ctakes

import java.util.Date

import org.apache.commons.cli.{Options, ParseException, PosixParser}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.feature.HashingTF
import twitter4j._
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

/**
 * Moddified and borrowed from databricks spark tutorial.
 */
object Utils {

  /**
   * Glue code for declarative testing of args. See impl example in Driver class.
   * verifies that each input in the string passes the tester function.
   * prints error and exists if not.
   */
  def checkpoint(
                  tester : Any => Boolean,
                  error : Any => Unit,
                  inputs: List[String]): Unit = {
    System.out.println("~~~~~~ Checkpoint ~~~~~")
    def test(failures:Int, tests : List[String]):Boolean= {
      tests match {
        case List() => {
          return failures == 0
        }
        case other => {
          return test(failures + {
            if (tester(tests.head)){0} else {
              error(tests.head)
              1
            }
          },
          tests.tail)
        }
      }
      tester(tests.head)
    }
   if ( ! test(0,inputs) )
     System.exit(2)
  }

  def getAuth = {
    Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  }

  /**
   * Create feature vectors by turning each tweet into bigrams of characters (an n-gram model)
   * and then hashing those to a length-1000 feature vector that we can pass to MLlib.
   * This is a common way to decrease the number of features in a model while still
   * getting excellent accuracy (otherwise every pair of Unicode characters would
   * potentially be a feature).
   */
  def featurize(s: String): Vector = {
    tf.transform(s.sliding(2).toSeq)
  }

  object IntParam {
    def unapply(str: String): Option[Int] = {
      try {
        Some(str.toInt)
      } catch {
        case e: NumberFormatException => None
      }
    }
  }


  /**
   * A Mock status object for testing twitter streaming w/o
   * actually connecting to twitter.
   */
  def mockStatus: Status = {
    new Status {

      override def getPlace: Place = ???

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
}
