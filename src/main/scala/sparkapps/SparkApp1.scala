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
  def sparkJob() = {

      val logFile = "/etc/passwd" // Should be some file on your system
      val conf = new SparkConf()
          .setAppName("Simple Application")
          //this needs to be parameterized.
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


    def main(args: Array[String]) {
        if(args.length == 0)
          System.err.println("Failing: No args.")
          System.exit(1);

        Integer.parseInt(args(0)) match {
          case xx if xx <= 100 => sparkJob();
          case xx if xx > 100 => System.err.println("Failing, too much. just prototype");
        }
    }
 }
