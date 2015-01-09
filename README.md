This is a spark SBT Template.

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
