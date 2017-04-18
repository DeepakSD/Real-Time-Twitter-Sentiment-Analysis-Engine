# Real-Time-Twitter-Sentiment-Analysis-Engine
This program incorporates the framework using Apache Spark Streaming, Kafka, Elasticsearch and Kibana. The framework performs Sentiment analysis of particular hash tags from twitter data in real-time. For example, we want to do the sentiment analysis for all the tweets for #trump, #obama and show them (e.g., positive, neutral, negative, etc.) on a map. When we show tweets on a map, we plot them using their latitude and longitude.

# Functionality
* Retrieve tweets using Spark Streaming

* Sentiment analysis (Stanford CoreNLP)

* Index tweets in Elasticsearch

* Live dashboard using Kibana

## Procedure
Step 1: Firstly, start zookeeper server and kafka server using the following commands,
  
    bin/zookeeper-server-start.sh config/zookeeper.properties
    bin/kafka-server-start.sh config/server.properties

Step 2: Then, create a topic for kafka streaming using the following command,

    bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tweets  

Step 3: Next, start the elasticsearch and kibana using the following commands,

    bin/elasticsearch
    bin/kibana

Step 4: Create an index pattern in elasticsearch using kibana console(localhost:5601/). Index pattern is available in "index_mapping.txt".

Step 5: Run the TwitterScrapper.java program and then run the sentimentAnalysis.scala program.
Sentiments are detected using Stanford CoreNLP. The latitudes and longitudes are retrieved using Google Geocode API(gson-2.3.1).

Step 6: Go to kibana console, and create visualizations and dashboard to view the output(PFA: Dashboard.png)





