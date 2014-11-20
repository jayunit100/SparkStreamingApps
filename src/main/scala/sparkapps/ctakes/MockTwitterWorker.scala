package sparkapps.ctakes

/**
 * Created by jayunit100 on 11/16/14.
 */
class MockTwitterWorker(callback:(twitter4j.Status)=>Unit) {

    def run() = {
      //keep running , kill it in onStop(), so that we mimick a real world stream.
      (1 to 100000).toStream.takeWhile(x => x < 5).foreach(
        i => {
          System.out.println("storing mock status " + i)
          Thread.sleep(100);
          //callback(newStatus(i))
          //store(newStatus(i))
        })
    }

}
