package org.ctrip.ops.sysdev.inputs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.decoder.IDecode;
import org.ctrip.ops.sysdev.decoder.JsonDecoder;
import org.ctrip.ops.sysdev.decoder.PlainDecoder;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class Kafka extends BaseInput {
	private static final Logger logger = Logger
			.getLogger(Kafka.class.getName());

	private class Consumer implements Runnable {
		private KafkaStream<byte[], byte[]> m_stream;
		private ArrayBlockingQueue messageQueue;
		private IDecode decoder;

		public Consumer(KafkaStream<byte[], byte[]> a_stream,
				ArrayBlockingQueue fairQueue, IDecode decoder) {
			m_stream = a_stream;
			this.messageQueue = fairQueue;
			this.decoder = decoder;
		}

		public void run() {
			ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
			while (it.hasNext()) {
				String m = new String(it.next().message());
				try {
					Map<String, Object> event = decoder.decode(m);
					this.messageQueue.put(event);
				} catch (InterruptedException e) {
					logger.warn("put message to queue failed");
					logger.trace(e.getMessage());
				} catch (Exception e) {
					logger.error("json decode failed:" + m);
					logger.trace(e.getMessage());
				}
			}
		}
	}

	private int threads;
	private String fetchers;
	private String topic;
	private ConsumerConnector consumer;
	private ExecutorService executor;
	private IDecode decoder;

	public Kafka(Map<String, Object> config, ArrayBlockingQueue messageQueue) {
		super(config, messageQueue);
	}

	@Override
	protected void prepare() {
		if (this.config.containsKey("threads")) {
			this.threads = (int) this.config.get("threads");
		} else {
			this.threads = 1;
		}
		
		if (this.config.containsKey("fetchers")) {
			this.fetchers = (String) this.config.get("fetchers");
		} else {
			this.fetchers = "1";
		}

		this.topic = (String) this.config.get("topic");

		String sessionTimeout = "6000", syncTime = "2000", commitInterval = "60000";
		if (config.containsKey("session_timeout")) {
			sessionTimeout = (String) config.get("session_timeout");
		}
		if (config.containsKey("sync_time")) {
			syncTime = (String) config.get("sync_time");
		}
		if (config.containsKey("commit_interval")) {
			commitInterval = (String) config.get("commit_interval");
		}

		Properties props = new Properties();
		props.put("zookeeper.connect", this.config.get("zk"));
		props.put("group.id", this.config.get("groupID"));
		props.put("zookeeper.session.timeout.ms", sessionTimeout);
		props.put("zookeeper.sync.time.ms", syncTime);
		props.put("auto.commit.interval.ms", commitInterval);
		props.put("num.consumer.fetchers", fetchers);

		consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(new ConsumerConfig(props));

		String codec = (String) this.config.get("codec");
		if (codec != null && codec.equalsIgnoreCase("plain")) {
			this.decoder = new PlainDecoder();
		} else {
			this.decoder = new JsonDecoder();
		}

	}

	public void emit() {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(this.topic, this.threads);
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = null;

		consumerMap = consumer.createMessageStreams(topicCountMap);

		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

		executor = Executors.newFixedThreadPool(this.threads);

		// now create an object to consume the messages

		for (final KafkaStream<byte[], byte[]> stream : streams) {
			executor.submit(new Consumer(stream, messageQueue, this.decoder));
		}
	}
}
