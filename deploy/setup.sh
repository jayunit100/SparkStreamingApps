echo "Getting tarballs and caching them in docker directories."

docker build -t jvm .

wget http://apache.cs.utah.edu/cassandra/2.0.11/apache-cassandra-2.0.11-bin.tar.gz -O cassandra/apache-cassandra-2.0.11-bin.tar.gz

wget http://d3kbcqa49mib13.cloudfront.net/spark-1.2.0-bin-hadoop2.4.tgz -O spark/spark-1.2.0-bin-hadoop2.4.tgz
