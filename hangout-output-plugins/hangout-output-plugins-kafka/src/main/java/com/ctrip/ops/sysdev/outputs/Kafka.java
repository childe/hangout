package com.ctrip.ops.sysdev.outputs;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import lombok.extern.log4j.Log4j;
import org.json.simple.JSONValue;

@Log4j
public class Kafka extends BaseOutput {

    private Producer producer;
    private String topic;
    private Properties props;

    public Kafka(Map config) {
        super(config);
    }

    protected void prepare() {
        if (!this.config.containsKey("topic")) {
            log.error("topic must be included in config");
            System.exit(1);
        }
        this.topic = (String) this.config.get("topic");

        props = new Properties();

        if (!this.config.containsKey("bootstrap_servers")) {
            log.error("bootstrap_servers must be included in producer_settings");
            System.exit(1);
        }
        props.put("bootstrap.servers", (String) this.config.get("bootstrap_servers"));

        HashMap<String, String> producerSettings = (HashMap<String, String>) this.config.get("producer_settings");
        if (producerSettings != null) {
            producerSettings.entrySet().stream().forEach(entry -> {
                String k = entry.getKey();
                String v = entry.getValue();
                props.put(k, v);
            });
        }

        if (props.get("key.serializer") == null) {
            props.put("key.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");
        }
        if (props.get("value.serializer") == null) {
            props.put("value.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");
        }
        producer = new KafkaProducer<>(props);
    }

    protected void emit(Map event) {
        producer.send(new ProducerRecord<String, String>(topic, JSONValue.toJSONString(event)));
    }

    public void shutdown() {
        log.info("close producer and then shutdown");
        producer.close();
    }
}
