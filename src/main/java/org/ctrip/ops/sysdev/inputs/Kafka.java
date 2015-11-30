package org.ctrip.ops.sysdev.inputs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class Kafka extends BaseInput {
	private static final Logger logger = Logger
			.getLogger(Kafka.class.getName());

	private ConsumerConnector consumer;
	private ExecutorService executor;
	private String encoding;

	private class Consumer implements Runnable {
		private KafkaStream<byte[], byte[]> m_stream;
		private Kafka kafkaInput;

		public Consumer(KafkaStream<byte[], byte[]> a_stream, Kafka kafkaInput) {
			this.m_stream = a_stream;
			this.kafkaInput = kafkaInput;
		}

		public void run() {
			ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
			while (it.hasNext()) {
				String m = null;
				try {
					m = new String(it.next().message(),
							this.kafkaInput.encoding);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					logger.error(e1);
				}

				Map<String, Object> event;
				try {
					this.kafkaInput.process(m);
				} catch (Exception e) {
					logger.error("process event failed:" + m);
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
	}

	public Kafka(Map<String, Object> config, ArrayList<Map> filter,
			ArrayList<Map> outputs) throws Exception {
		super(config, filter, outputs);
		this.prepare();
	}

	@SuppressWarnings("unchecked")
	protected void prepare() {
		Properties props = new Properties();

		HashMap<String, String> consumerSettings = (HashMap<String, String>) this.config
				.get("consumer_settings");
		Iterator<Entry<String, String>> consumerSetting = consumerSettings
				.entrySet().iterator();

		while (consumerSetting.hasNext()) {
			Map.Entry<String, String> entry = consumerSetting.next();
			String k = entry.getKey();
			String v = entry.getValue();
			props.put(k, v);
		}
		consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(new ConsumerConfig(props));

		if (this.config.containsKey("encoding")) {
			this.encoding = (String) this.config.get("encoding");
		} else {
			this.encoding = "UTF8";
		}
	}

	public void emit() {
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = null;

		Map<String, Integer> topics = (Map<String, Integer>) this.config
				.get("topic");
		consumerMap = consumer.createMessageStreams(topics);

		Iterator<Entry<String, Integer>> topicIT = topics.entrySet().iterator();

		while (topicIT.hasNext()) {
			Map.Entry<String, Integer> entry = topicIT.next();
			String topic = entry.getKey();
			Integer threads = entry.getValue();
			List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

			executor = Executors.newFixedThreadPool(threads);

			for (final KafkaStream<byte[], byte[]> stream : streams) {
				executor.submit(new Consumer(stream, this));
			}
		}
	}
}
