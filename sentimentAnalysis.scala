package org.streaming.Tweet_Sentiment_Analysis

import java.util.HashMap
import java.util.Properties
import java.time.format.DateTimeFormatter

import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.{ SparkContext, SparkConf }
import org.apache.spark.storage.StorageLevel

import org.elasticsearch.spark._
import org.streaming.Tweet_Sentiment_Analysis.stanfordNLP._

object sentimentAnalysis {

  val conf = new SparkConf().setMaster("local[*]").setAppName("Sentiment Analysis").set("spark.executor.memory", "1g")
  conf.set("es.nodes", "localhost:9200")
  //conf.set("es.index.auto.create", "true")
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {

    sc.setLogLevel("WARN")

    val Array(zooKeeper, group, topics, numThreads) = args
    val streamingContext = new StreamingContext(sc, Seconds(120))
    streamingContext.checkpoint("checkpoint")
 
    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap     
    val tweets = KafkaUtils.createStream(streamingContext, zooKeeper, group, topicMap).map(_._2)

    tweets.foreachRDD { (rdd, time) =>
      rdd.map(t => {
        Map(
          "hashtag" -> t.split("::concat::")(0),
          "sentiment" -> analyzeSentiment(t.split("::concat::")(0)).toString,
          "geo_location" -> Array(t.split("::concat::")(2).toDouble, t.split("::concat::")(1).toDouble),
          "time" -> t.split("::concat::")(3))
      }).saveToEs("twitter/tweetDetails")
    }
    
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def getHashTags(line: String): String = {
    val temp = line.toLowerCase()
    if ((temp contains "trump") && (temp contains "obama")) {
      return "#trump"
    } else if (temp contains "trump") {
      return "#trump"
    } else if (temp contains "obama") {
      return "#obama"
    } else
      return "#undefined"
  }
}
