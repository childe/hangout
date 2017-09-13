package com.ctrip.ops.sysdev.outputs;

import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

public class Kafka extends BaseOutput {
	private static final Logger logger = Logger
			.getLogger(Kafka.class.getName());

	private Producer producer;
	private String topic;

	public Kafka(Map config) {
		super(config);
	}

	protected void prepare() {
		Properties props = new Properties();
		props.put("metadata.broker.list",
				(String) this.config.get("broker_list"));
		props.put("key.serializer.class", "kafka.serializer.StringEncoder");
		props.put("request.required.acks", "0");
		ProducerConfig pconfig = new ProducerConfig(props);
		this.topic = (String) this.config.get("topic");
		producer = new Producer<>(pconfig);
	};

	protected void emit(Map event) {
		producer.send(new KeyedMessage<>(topic, JSONValue.toJSONString(event)
				.getBytes()));
	}
}
