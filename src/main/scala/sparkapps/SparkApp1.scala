package sparkapps

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD


/**
 * Created by apache on 7/20/14.
 */
object SparkApp1 {
  /* SimpleApp.scala */

    def merger() = {

      val sets = collection.mutable.ArrayBuffer(Set(1,2,3,4,5,6))

      var i = 0

      while (i < sets.size) {
        val index = sets.tail.indexWhere(s => (s & sets.head).size > 0)
        if (index >= 0) {
          sets += (sets.remove(0) | sets.remove(index))
        }
        i += 1
      }

    }


  def sparkJob() = {

    val logFile = "/etc/passwd" // Should be some file on your system
    val conf = new SparkConf()
        .setAppName("Simple Application")
        .setMaster("local")

    val sc = new SparkContext(conf)

    val logData = sc.textFile(logFile, 2).cache()

    val numAs =
      logData.filter(line => line.contains("a")).count()

    val numBs =
      logData.filter(line => line.contains("b")).count()

    val piped = logData.pipe("grep a").collect();

    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
  }

  def monad(){



  }

    def main(args: Array[String]) {
      if(args.length==0)
        merger();
      else {
        args(0) match   {
        case "1" => sparkJob();
        case "2" => merger();
        case "3" => monad();
        }
    }
    }

}
