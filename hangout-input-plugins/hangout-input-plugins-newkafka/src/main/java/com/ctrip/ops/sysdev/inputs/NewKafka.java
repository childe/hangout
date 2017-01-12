package com.ctrip.ops.sysdev.inputs;

/**
 * Created by liujia on 16/4/1.
 */

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import com.ctrip.ops.sysdev.decoders.IDecode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

@SuppressWarnings("ALL")
public class NewKafka extends BaseInput {
    private static final Logger logger = Logger
            .getLogger(NewKafka.class.getName());

    private ExecutorService executor;

    public NewKafka(Map<String, Object> config, ArrayList<Map> filter,
                 ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
    }

    private class Consumer implements Runnable {
        private NewKafka kafkaInput;
        private IDecode decoder;
        private KafkaConsumer<String, String> consumer;
        private BaseFilter[] filterProcessors;
        private BaseOutput[] outputProcessors;

        public Consumer(String topicName, NewKafka kafkaInput) {
            this.kafkaInput = kafkaInput;
            this.decoder = kafkaInput.createDecoder();

            Properties props = new Properties();

            HashMap<String, String> consumerSettings = (HashMap<String, String>) this.kafkaInput.config
                    .get("consumer_settings");
            Iterator<Map.Entry<String, String>> consumerSetting = consumerSettings
                    .entrySet().iterator();

            while (consumerSetting.hasNext()) {
                Map.Entry<String, String> entry = consumerSetting.next();
                String k = entry.getKey();
                String v = entry.getValue();
                props.put(k, v);
            }
            this.consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(topicName));

            this.filterProcessors = kafkaInput.createFilterProcessors();
            this.outputProcessors = kafkaInput.createOutputProcessors();
        }

        public void run() {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records)
                    try {
                        Map<String, Object> event = this.decoder
                                .decode(record.value());

                        if (this.filterProcessors != null) {
                            for (BaseFilter bf : filterProcessors) {
                                if (event == null) {
                                    break;
                                }
                                event = bf.process(event);
                            }
                        }
                        if (event != null) {
                            for (BaseOutput bo : outputProcessors) {
                                bo.process(event);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("process event failed:" + record.value());
                        e.printStackTrace();
                        logger.error(e);
                    }
            }
        }
    }


    protected void prepare() {
    }

    public void emit() {
        Map<String, Integer> topics = (Map<String, Integer>) this.config
                .get("topic");

        Iterator<Map.Entry<String, Integer>> topicIT = topics.entrySet().iterator();

        while (topicIT.hasNext()) {
            Map.Entry<String, Integer> entry = topicIT.next();

            String topic = entry.getKey();
            Integer threads = entry.getValue();

            executor = Executors.newFixedThreadPool(threads);

            for (int i = 0; i < threads; i++) {
                executor.submit(new Consumer(topic, this));
            }
        }
    }
}
