package com.ctrip.ops.sysdev.inputs;

/**
 * Created by liujia on 16/4/1.
 * Modifiled by gnuhpc on 17/2/8
 */

import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import lombok.extern.log4j.Log4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Log4j
public class NewKafka extends BaseInput {

    private ExecutorService executor;
    private Map<String, Integer> topics;
    private Properties props;
    private Map<String, Integer> topicPatterns;

    public NewKafka(Map<String, Object> config, ArrayList<Map> filter,
                 ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
    }


    protected void prepare() {
        topics = (Map<String, Integer>) this.config.get("topic");
        topicPatterns = (Map<String,Integer>) this.config.get("topic_pattern");
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
        if(topicPatterns!=null){
            topicPatterns.entrySet().stream().forEach(entry->{
                Pattern topicPattern = Pattern.compile(entry.getKey());
                int threadSize = entry.getValue();
                executor = Executors.newFixedThreadPool(threadSize);
                for (int i = 0; i < threadSize; i++) {
                    KafkaConsumer consumer = new KafkaConsumer<>(props);
                    executor.submit(new ConsumerThread(topicPattern,consumer));
                }
            });

        }else {
            //Create Consumer Streams Map
            topics.entrySet().stream().forEach(entry-> {
                String topic = entry.getKey();
                int threadSettingSize = entry.getValue();
                KafkaConsumer consumer = new KafkaConsumer<>(props);
                int partitionSize = consumer.partitionsFor(topic).size();
                int threadSize;
                //If threadSettingSize > partitionSize, use paritionSize in order to avoid the waste of threads
                if (partitionSize < threadSettingSize) {
                    threadSize = partitionSize;
                } else {
                    threadSize = threadSettingSize;
                }

                executor = Executors.newFixedThreadPool(threadSize);
                for (int i = 0; i < threadSize; i++) {
                    //One KafkaConsumer instance per thread
                    executor.submit(new ConsumerThread(topic, new KafkaConsumer<>(props)));
                }
            });
        }
    }

    private class ConsumerThread implements Runnable {

        private final KafkaConsumer<String,String> consumer;

        public ConsumerThread(String topicName, KafkaConsumer consumer) {
            this.consumer = consumer;
            this.consumer.subscribe(Arrays.asList(topicName));
        }

        public ConsumerThread(Pattern topicPattern, KafkaConsumer consumer) {

            this.consumer = consumer;
            this.consumer.subscribe(topicPattern, new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    partitions.forEach(partition->{
                        log.info("Rebalance happened"+partition.topic()+":"+partition.partition());
                    });
                }
            });
        }

        public void run() {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(10000);
                for (ConsumerRecord<String, String> record : records)
                        applyProcessor(record.value());
            }
        }
    }
}
