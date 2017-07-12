package com.ctrip.ops.sysdev.inputs;

/**
 * Created by liujia on 16/4/1.
 * Modifiled by gnuhpc on 17/2/11
 */

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Log4j2
public class NewKafka extends BaseInput {

    private ExecutorService executor;
    private Map<String, Integer> topics;
    private Properties props;
    private Map<String, Integer> topicPatterns;
    private ArrayList<ConsumerThread> consumerThreadsList = new ArrayList<>();

    public NewKafka(Map<String, Object> config, ArrayList<Map> filter,
                    ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
    }


    protected void prepare() {
        topics = (Map<String, Integer>) this.config.get("topic");
        topicPatterns = (Map<String, Integer>) this.config.get("topic_pattern");
        HashMap<String, String> consumerSettings = (HashMap<String, String>) this.config.get("consumer_settings");
        props = new Properties();

        consumerSettings.entrySet().stream().forEach(entry -> {
            String k = entry.getKey();
            String v = entry.getValue();
            props.put(k, v);
        });
    }

    public void emit() {
        if (topicPatterns != null) {
            topicPatterns.entrySet().stream().forEach(entry -> {
                Pattern topicPattern = Pattern.compile(entry.getKey());
                int threadSize = entry.getValue();
                executor = Executors.newFixedThreadPool(threadSize);
                for (int i = 0; i < threadSize; i++) {
                    ConsumerThread consumerThread = new ConsumerThread(topicPattern,props,this);
                    consumerThreadsList.add(consumerThread);
                    executor.submit(consumerThread);
                }
            });

        } else {
            //Create Consumer Streams Map
            topics.entrySet().stream().forEach(entry -> {
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
                    executor.submit(new ConsumerThread(topic, props, this));
                }
            });
        }
    }

    @Override
    public void shutdown() {
        consumerThreadsList.forEach(consumerThread -> consumerThread.shutdown());
        executor.shutdown();
        try {
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    private class ConsumerThread implements Runnable {

        private KafkaConsumer<String, String> consumer;
        private List<BaseFilter> filterProcessors;
        private List<BaseOutput> outputProcessors;

        public ConsumerThread(String topicName, Properties props, NewKafka kafka) {
            consumer = new KafkaConsumer<>(props);
            initConsumerThread(props,kafka);
            this.consumer.subscribe(Arrays.asList(topicName));
        }

        public ConsumerThread(Pattern topicPattern, Properties props, NewKafka kafka) {
            initConsumerThread(props,kafka);
            this.consumer.subscribe(topicPattern, new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    partitions.forEach(partition -> {
                        log.info("Rebalance happened " + partition.topic() + ":" + partition.partition());
                    });
                }
            });
        }

        public void initConsumerThread(Properties prop, NewKafka kafka){
            this.consumer = new KafkaConsumer<>(props);
            this.filterProcessors = kafka.createFilterProcessors();
            this.outputProcessors = kafka.createOutputProcessors();
        }

        public void run() {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(10000);
                for (ConsumerRecord<String, String> record : records)
                    process(record.value(), this.filterProcessors, this.outputProcessors);
            }
        }

        public void shutdown() {
            consumer.wakeup();
            consumer.close();
        }
    }
}
