package com.ctrip.ops.sysdev.inputs;

/**
 * Created by liujia on 16/4/1.
 * Modifiled by gnuhpc on 17/2/8
 */

import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import lombok.extern.log4j.Log4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j
public class NewKafka extends BaseInput {

    private ExecutorService executor;
    private Map<String, Integer> topics;
    private Properties props;

    public NewKafka(Map<String, Object> config, ArrayList<Map> filter,
                 ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
    }


    protected void prepare() {
        topics = (Map<String, Integer>) this.config.get("topic");
        HashMap<String, String> consumerSettings = (HashMap<String, String>) this.config.get("consumer_settings");
        props = new Properties();

        consumerSettings.entrySet().stream().forEach(entry->{
            String k = entry.getKey();
            String v = entry.getValue();
            props.put(k, v);
        });

        createProcessors();
    }

    public void emit() {
        topics.entrySet().stream().forEach(entry->{
            String topic = entry.getKey();
            KafkaConsumer consumer = new KafkaConsumer<>(props);
            int partitionSize = consumer.partitionsFor(topic).size();
            int threadSize = entry.getValue();

            //If threadSize > partitionSize, use paritionSize in order to avoid the waste of threads
            if (partitionSize<threadSize){
                executor = Executors.newFixedThreadPool(partitionSize);
            }
            else{
                executor = Executors.newFixedThreadPool(threadSize);
            }

            for (int i = 0; i < partitionSize; i++) {
                executor.submit(new ConsumerThread(topic,consumer));
            }
        });
    }

    private class ConsumerThread implements Runnable {

        private final KafkaConsumer<String,String> consumer;

        public ConsumerThread(String topicName, KafkaConsumer consumer) {
            this.consumer = consumer;
            this.consumer.subscribe(Arrays.asList(topicName));
        }

        public void run() {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(10000);
                for (ConsumerRecord<String, String> record : records)
                        process(record.value());
            }
        }
    }
}
