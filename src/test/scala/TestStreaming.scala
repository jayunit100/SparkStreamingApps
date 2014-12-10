import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf
import sparkapps.tweetstream.{TwitterAppTemplate, TwitterStreamingApp, MockInputDStream, Processor}

/**
 *
 * A unit test that tests just the
 * stream processor.
 *
 * This test works by
 * 1) creating mock cassandra db, and
 * 2) inserting data into it using a mock dstream (generates data).
 * 3) ETLs function is sent as an argument to the Processor.
 *
 */
class TestTwitterETL {


  /**
   * Add other examples.... i.e. hbase ETL, here.
   */

  /**
   * Here is an implementation of Cassandra based ETL.
   */
  @org.junit.Test
  def testCassandraETL(){
    /**
     * FYI This should fail fast for you if cassandra isnt set up right :).
     * make sure and turn off iptables if you get a "operation timed out" exception.
     */
    def startCassandraStream() = {
      val conf = new SparkConf()
        .setAppName(this.getClass.getSimpleName + "" + System.currentTimeMillis())
        .setMaster("local[2]")
        .set("spark.cassandra.connection.host", "127.0.0.1")
        .set("spark.cassandra.connection.native.port","9042")

      CassandraConnector(conf).withSessionDo {
        session =>
          session.execute(s"CREATE KEYSPACE IF NOT EXISTS streaming_test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 3 }")
          session.execute(s"CREATE TABLE IF NOT EXISTS streaming_test.key_value (key VARCHAR PRIMARY KEY, value INT)")
          session.execute(s"TRUNCATE streaming_test.key_value")
      }


      TwitterAppTemplate.startStream(
        conf,
        sparkapps.tweetstream.MockInputDStream(1)_, // <-- how to make this curried?
        {
          (transactions,sparkConf) =>
            //assumes session.
            CassandraConnector(sparkConf).withSessionDo {
              session => {
                val x=1
                Thread.sleep(1)
                transactions.foreach({
                  xN =>
                    val xNtxt=xN.toString+" "+xN.getText;
                    session.executeAsync(s"INSERT INTO streaming_test.key_value (key, value) VALUES ('$xNtxt' , $x)")}
                )
                true;
              }
            }
        })
    }

    startCassandraStream()
  }

}
