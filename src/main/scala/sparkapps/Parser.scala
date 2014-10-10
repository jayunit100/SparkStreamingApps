package sparkapps

import scala.collection.SortedMap
import scala.collection.immutable._;
/**
 * Created by jay on 10/9/14.
 */
object Parser {



  val usage =
    """
      |      System.setProperty("twitter4j.oauth.consumerKey", cl.getOptionValue(CONSUMER_KEY))
      |      System.setProperty("twitter4j.oauth.consumerSecret", cl.getOptionValue(CONSUMER_SECRET))
      |      System.setProperty("twitter4j.oauth.accessToken", cl.getOptionValue(ACCESS_TOKEN))
      |      System.setProperty("twitter4j.oauth.accessTokenSecret", cl.getOptionValue(ACCESS_TOKEN_SECRET))
    """

  type OptionMap = TreeMap[String,String]

  def nextOption(map : OptionMap, list : List[String]): OptionMap = {
    def isSwitch(s:String)=(s.substring(0,1).equals("--"))
      list match {
        case Nil => map
        case "--outputDirectory" :: value :: tail => nextOption(map++Map("outputDirectory"->value),tail)
        case "--numtweets" :: value :: tail => nextOption(map++Map("numtweets"->value),tail)
        case "--intervals" :: value :: tail => nextOption(map++Map("interval"->value),tail)
        case "--partitions" :: value :: tail => nextOption(map++Map("partitions"->value),tail)
        case unknown :: value :: tail => System.out.println("Setting sys prop " + unknown + " "+value)
                                         System.setProperty(unknown,value)
                                         nextOption(map,tail)
      }
  }

  def parse(args:Array[String]) : Array[String] = {
    val iter = nextOption(new TreeMap(),args.toList).valuesIterator
    Array(
      iter.next(),
      iter.next(),
      iter.next(),
      iter.next()) ;
  }


}
