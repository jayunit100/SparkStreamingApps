package sparkapps.ctakes

import org.apache.commons.cli.{Options, ParseException, PosixParser}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.feature.HashingTF
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

/**
 * Moddified and borrowed from databricks spark tutorial.
 */
object Utils {

  val numFeatures = 1000
  val tf = new HashingTF(numFeatures)

  val CONSUMER_KEY = "consumerKey"
  val CONSUMER_SECRET = "consumerSecret"
  val ACCESS_TOKEN = "accessToken"
  val ACCESS_TOKEN_SECRET = "accessTokenSecret"

  val THE_OPTIONS = {
    val options = new Options()
    options.addOption(CONSUMER_KEY, true, "Twitter OAuth Consumer Key")
    options.addOption(CONSUMER_SECRET, true, "Twitter OAuth Consumer Secret")
    options.addOption(ACCESS_TOKEN, true, "Twitter OAuth Access Token")
    options.addOption(ACCESS_TOKEN_SECRET, true, "Twitter OAuth Access Token Secret")
    options
  }

  /**
   * Test several arguments.
   * Partially this is an experiment w/ scala syntax :)
   * If any tests fail, an error condition is called.
   * At the end, if failure, the System exits.
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

  /**
   * Modifications...
   */
  def parseCommandLineWithTwitterCredentials(args: Array[String]) : Option[Array[AnyRef]] = {
    val parser = new PosixParser
    try {
      val cl = parser.parse(THE_OPTIONS, args)
      /**
       * Parse twitter args and put them on system properties.
       */
      System.setProperty("twitter4j.oauth.consumerKey", cl.getOptionValue(CONSUMER_KEY))
      System.setProperty("twitter4j.oauth.consumerSecret", cl.getOptionValue(CONSUMER_SECRET))
      System.setProperty("twitter4j.oauth.accessToken", cl.getOptionValue(ACCESS_TOKEN))
      System.setProperty("twitter4j.oauth.accessTokenSecret", cl.getOptionValue(ACCESS_TOKEN_SECRET))
      System.out.println("Returning " + cl.getArgList.toArray());
      /**
       * Return the rest of the arguments as a list.
       */
      Some(cl.getArgList.toArray())
    }
    catch {
      case e: ParseException =>
        System.err.println("Parsing failed.  Reason: " + e.getMessage)
        None
    }
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
}
