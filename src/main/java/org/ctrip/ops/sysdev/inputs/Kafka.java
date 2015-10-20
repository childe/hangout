package org.ctrip.ops.sysdev.inputs;

import java.lang.reflect.Constructor;
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
import org.ctrip.ops.sysdev.decoder.IDecode;
import org.ctrip.ops.sysdev.decoder.JsonDecoder;
import org.ctrip.ops.sysdev.decoder.PlainDecoder;
import org.ctrip.ops.sysdev.filters.BaseFilter;
import org.ctrip.ops.sysdev.outputs.BaseOutput;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class Kafka extends BaseInput {
	private static final Logger logger = Logger
			.getLogger(Kafka.class.getName());

	private class Consumer implements Runnable {
		private KafkaStream<byte[], byte[]> m_stream;
		private IDecode decoder;
		private BaseFilter[] filterProcessors;
		private BaseOutput[] outputProcessors;

		public Consumer(KafkaStream<byte[], byte[]> a_stream, IDecode decoder,
				BaseFilter[] filterProcessors, ArrayList<Map> outputs) {
			m_stream = a_stream;
			this.decoder = decoder;
			this.filterProcessors = filterProcessors.clone();

			outputProcessors = new BaseOutput[outputs.size()];

			int idx = 0;
			for (Map output : outputs) {
				Iterator<Entry<String, Map>> outputIT = output.entrySet()
						.iterator();

				while (outputIT.hasNext()) {
					Map.Entry<String, Map> outputEntry = outputIT.next();
					String outputType = outputEntry.getKey();
					Map outputConfig = outputEntry.getValue();
					Class<?> outputClass;
					try {
						outputClass = Class
								.forName("org.ctrip.ops.sysdev.outputs."
										+ outputType);
						Constructor<?> ctor = outputClass
								.getConstructor(Map.class);

						outputProcessors[idx] = (BaseOutput) ctor
								.newInstance(outputConfig);
						idx++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void run() {
			ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
			while (it.hasNext()) {
				String m = new String(it.next().message());
				Map<String, Object> event;
				try {
					event = decoder.decode(m);

					for (BaseFilter bf : filterProcessors) {
						if (event == null) {
							break;
						}
						event = bf.process(event);
					}
					if (event != null) {
						for (BaseOutput bo : outputProcessors) {
							bo.process(event);
						}
					}
				} catch (Exception e) {
					logger.error("process event failed:" + m);
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
	}

	private ConsumerConnector consumer;
	private ExecutorService executor;
	private IDecode decoder;
	private Map<String, Integer> topic;

	public Kafka(Map<String, Object> config, BaseFilter[] filterProcessors,
			ArrayList<Map> outputs) {
		super(config, filterProcessors, outputs);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void prepare() {
		this.topic = (Map<String, Integer>) this.config.get("topic");

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

		String codec = (String) this.config.get("codec");
		if (codec != null && codec.equalsIgnoreCase("plain")) {
			this.decoder = new PlainDecoder();
		} else {
			this.decoder = new JsonDecoder();
		}
	}

	public void emit() {
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = null;

		consumerMap = consumer.createMessageStreams(this.topic);

		Iterator<Entry<String, Integer>> topicIT = this.topic.entrySet()
				.iterator();

		while (topicIT.hasNext()) {
			Map.Entry<String, Integer> entry = topicIT.next();
			String topic = entry.getKey();
			Integer threads = entry.getValue();
			List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

			executor = Executors.newFixedThreadPool(threads);

			for (final KafkaStream<byte[], byte[]> stream : streams) {
				executor.submit(new Consumer(stream, this.decoder,
						this.filterProcessors, this.outputs));
			}
		}

	}
}
