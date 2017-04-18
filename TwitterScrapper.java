package org.streaming.Tweet_Sentiment_Analysis;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.*;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.streaming.Tweet_Sentiment_Analysis.ApiRequest;

public class TwitterScrapper {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);

		if (args.length < 4) {
			System.out.println(
					"Usage: TwitterScrapper <consumer-key> <consumer-secret> <access-token> <access-token-secret> <topic-name> <search-keywords>");
			return;
		}

		String consumerKey = args[0].toString();
		String consumerSecret = args[1].toString();
		String accessToken = args[2].toString();
		String accessTokenSecret = args[3].toString();
		String topic = args[4].toString();
		String[] arguments = args.clone();
		String[] keyWords = Arrays.copyOfRange(arguments, 5, arguments.length);

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);

		TwitterStream Stream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {
				queue.offer(status);
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			public void onScrubGeo(long userId, long upToStatusId) {
			}

			public void onStallWarning(StallWarning warning) {
			}

			public void onException(Exception ex) {
			}
		};
		Stream.addListener(listener);

		FilterQuery query = new FilterQuery().track(keyWords);
		Stream.filter(query);

		Properties props = new Properties();
		props.put("metadata.broker.list", "localhost:9092");
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<String, String>(props);
		int i = 0;

		while (true) {
			Status tweet = queue.poll();

			if (tweet == null) {
				Thread.sleep(100);
			} else {
				String line = tweet.getText().toLowerCase();
				if ((line.contains("trump") || line.contains("obama")) && tweet.getUser().getLocation() != null) {
					if (((tweet.getUser().getLocation()).matches("\\w*, \\w*"))
							|| ((tweet.getUser().getLocation()).matches("\\w* \\w*, \\w*"))) {
						System.out.println("Tweet:" + tweet.getText());
						System.out.println("Location:" + tweet.getUser().getLocation());
						// System.out.println("Date:
						// "+tweet.getCreatedAt().getTime());
						String coordinates = null;
						if ((coordinates = ApiRequest.coordinates(tweet.getUser().getLocation())) != null) {
							System.out.println("Coordinates:" + coordinates);
							String tweetDetails = tweet.getText() + "::concat::" + coordinates + "::concat::"
									+ tweet.getCreatedAt().getTime();
							producer.send(
									new ProducerRecord<String, String>(topic, Integer.toString(i++), tweetDetails));
						}
					}
				}
			}
		}
	}
}
