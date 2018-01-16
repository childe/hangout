package com.ctrip.ops.sysdev.outputs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.ctrip.ops.sysdev.render.TemplateRender;

import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONValue;

@Log4j2
public class Kafka extends BaseOutput {

    private Producer producer;
    private String topic;
    private TemplateRender format;
    private Properties props;
    private TemplateRender topicRender;

    public Kafka(Map config) {
        super(config);
    }

    protected void prepare() {
        if (!this.config.containsKey("topic")) {
            log.error("topic must be included in config");
            System.exit(1);
        }
        this.topic = (String) this.config.get("topic");
        try {
            this.topicRender = TemplateRender.getRender(topic);
        } catch (IOException e) {
            log.fatal("could not build template from" + topic);
            System.exit(1);
        }
        if (this.config.containsKey("format")) {
            String format = (String) this.config.get("format");
            try {
                this.format = TemplateRender.getRender(format);
            } catch (IOException e) {
                log.fatal("could not build template from" + format);
                System.exit(1);
            }
        } else {
            this.format = null;
        }
        props = new Properties();

        HashMap<String, String> producerSettings = (HashMap<String, String>) this.config.get("producer_settings");

        if (producerSettings != null) {
            producerSettings.entrySet().stream().forEach(entry -> {
                String k = entry.getKey();
                String v = entry.getValue();
                props.put(k, v);
            });
        } else {
            log.error("producer_settings must be included in config");
            System.exit(1);
        }

        if (props.get("bootstrap.servers") == null) {
            log.error("bootstrap.servers must be included in producer_settings");
            System.exit(1);
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
        String _topic = topicRender.render(event).toString();
        if (this.format == null) {
            producer.send(new ProducerRecord<String, String>(_topic, JSONValue.toJSONString(event)));
        } else {
            Object message = this.format.render(event);
            if (message != null) {
                producer.send(new ProducerRecord<String, String>(_topic, message.toString()));
            }
        }
    }

    public void shutdown() {
        log.info("close producer and then shutdown");
        producer.close();
    }
}
