package sparkapps.ctakes
import scala.collection.immutable._;
/**
 * A simple utility for parsing arguments.
 * As input, it takes the command line arguments.
 * It returns a list of ordered args for the program.
 * i.e. input is --a 1 --b 2 c 2---> output= 1 2 ...
 * It also sets SystemProperties as well for any args which
 * dont have switches (i.e. System.setProprety(c,2) will be called.)
 */
object Parser {

  val CONSUMER_KEY = "consumerKey"
  val CONSUMER_SECRET = "consumerSecret"
  val ACCESS_TOKEN = "accessToken"
  val ACCESS_TOKEN_SECRET = "accessTokenSecret"

  type OptionMap = TreeMap[String,String]

  //Returns a map of commandline options.
  //for unknown options, sets them as system properties, so that
  //arbitrary properties can be set.
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

  /**
   * Send in the arguments for this app to this method.  It will
   * return the args in order.
   */
  def parse(args:Array[String]) : Array[String] = {
    val iter = nextOption(new TreeMap(),args.toList).valuesIterator
    val returnVal = Array(
      iter.next(),
      iter.next(),
      iter.next(),
      iter.next()) ;
    System.out.println("Parsed inputs : " +returnVal)
    returnVal
  }
}