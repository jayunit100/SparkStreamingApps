/**
 * Created by jay on 10/28/14.
 */
class TestStreaming {

  @org.junit.Test
  def test(){
    sparkapps.tweetstream.Processor.startCassandraStream()
  }

}
