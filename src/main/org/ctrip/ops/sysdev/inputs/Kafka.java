package org.ctrip.ops.sysdev.inputs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import kafka.common.ConsumerRebalanceFailedException;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class Kafka extends BaseInput {
	private static final Logger logger = Logger.getLogger("InPutKafka");

	private class Consumer implements Runnable {
		private KafkaStream<byte[], byte[]> m_stream;
		private ArrayBlockingQueue messageQueue;

		public Consumer(KafkaStream<byte[], byte[]> a_stream,
				ArrayBlockingQueue fairQueue) {
			m_stream = a_stream;
			this.messageQueue = fairQueue;
		}

		public void run() {
			ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
			while (it.hasNext()) {
				String m = new String(it.next().message());
				try {
					this.messageQueue.put(m);
					// System.out.println(m);
				} catch (InterruptedException e) {
					logger.warn("put message to queue failed");
					logger.trace(e.getMessage());
				}
			}
		}
	}

	private Map<String, Object> config;
	private final int threads;
	private final String topic;
	private final int queueSize;
	private final ConsumerConnector consumer;
	private ExecutorService executor;
	private ArrayBlockingQueue messageQueue;

	public Kafka(Map<String, Object> config) {
		super(config);
		this.config = config;

		if (this.config.containsKey("threads")) {
			this.threads = (int) this.config.get("threads");
		} else {
			this.threads = 1;
		}
		if (this.config.containsKey("queueSize")) {
			this.queueSize = (int) this.config.get("queueSize");
		} else {
			this.queueSize = 1000;
		}

		this.messageQueue = new ArrayBlockingQueue(this.queueSize, false);

		this.topic = (String) this.config.get("topic");

		Properties props = new Properties();
		props.put("zookeeper.connect", this.config.get("zk"));
		props.put("group.id", "groupID");
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");

		consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(new ConsumerConfig(props));

	}

	public Map<String, Object> emit() {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(this.topic, this.threads);
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = null;
		try {
			consumerMap = consumer.createMessageStreams(topicCountMap);
		} catch (ConsumerRebalanceFailedException e) {
			logger.error(String.format(
					"ZookeeperConsumerConnector.consume %s can't rebalance",
					this.topic));
			logger.trace(e.getMessage());
			System.exit(1);
		}

		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

		executor = Executors.newFixedThreadPool(this.threads);

		// now create an object to consume the messages

		for (final KafkaStream<byte[], byte[]> stream : streams) {
			executor.submit(new Consumer(stream, messageQueue));
		}
		return null;
	}

	public ArrayBlockingQueue getMessageQueue() {
		return this.messageQueue;
	};

}
