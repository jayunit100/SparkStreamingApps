# TL;DR 

The spark streaming API used in this repo boils down to the following snippet which demonstrates how an RDD has
multiple Tweets in it (in this case, the tweets may have medical content, and that this blueprint implements a CTakes medical term extraction from an ongoing twitter stream, which uses the CTakes lexicon).  But in any case, the thing to note is that
RDDs of tweets have multiple tweets in them, so you have a nested processing loop, where in we (1) forEachRDD and (2) forEachTweet *in* the RDD.

```

    tweetStream.foreachRDD( transactions => {
        CassandraConnector(conf).withSessionDo {
        session => {
          val x=1
          Thread.sleep(1)
          transactions.foreach({
            xN =>
              System.out.println("Running Cassandra Insert..." + xN)
              System.out.println("Note that this can fail if cassandra isnt working...")
              val xNtxt=xN.toString+" "+xN.getText;
              session.executeAsync(s"INSERT INTO streaming_test.key_value (key, value) VALUES ('$xNtxt' , $x)")
          })
        }
      }
    })
```

# How to use this Repo

I used this as my Demonstration at apachecon a few years ago to show how to build spark streaming apps.

This repo has two different resources.

- testing of spark docker containers, orchestrated via vagrant.
- spark streaming blueprint applications.

If interested just in the docker containers, checkout the ```deploy/``` directory. 

These are mostly unrelated.  They just happen to be in the same repo. 

# Spark Streaming Blueprint apps. 

To use it, import it into INtelliJ or your favorite IDE as an SBT project.

Then you should be able to run standard SBT tests/compile tasks etc 
inside your idea and also in standalone mode.

1. git clone jayunit100/SparkBluePrint <YOUR APP NAME>

1. remove folders that dont apply to your project.

1. Now open intellij, and import.

1. Pick "SBT" project as the template

1. Run the Tester class via intelliJ

1. Change the .git/config to  point to your repository.

# Running in a real cluster 

- Set up a spark cluster w/ cassandra slaves.  There is a WIP project under deploy/ which sets scaffolding for this 
up using dockerfiles and vagrant to create a n-node spark cluster w/ a cassandra sink.  

- Then run ```sbt package```, to create the jar.

- copy the jar into the shared directory defined in the vagrantfile in deploy (in general, just copy the file 
into your machine that submits the spark jobs).

- spark-submit the application jar w/ desired class name (details coming soon).




Feedback welcome !
