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


    def main(args: Array[String]) {
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

      val piped = logData.pipe("grep a").collect() ;

      println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
   }

}
