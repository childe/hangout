package com.ctrip.ops.sysdev.inputs;

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
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.decoder.IDecode;
import com.ctrip.ops.sysdev.filters.BaseFilter;
import com.ctrip.ops.sysdev.outputs.BaseOutput;
import com.ctrip.ops.sysdev.monitor.SinkCounter;

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
        private IDecode decoder;
        private String encoding;
        private BaseFilter[] filterProcessors;
        private BaseOutput[] outputProcessors;
        private SinkCounter sc;

        public Consumer(KafkaStream<byte[], byte[]> a_stream, Kafka kafkaInput) {
            this.m_stream = a_stream;
            this.kafkaInput = kafkaInput;
            this.encoding = kafkaInput.encoding;
            this.decoder = kafkaInput.createDecoder();
            this.filterProcessors = kafkaInput.createFilterProcessors();
            this.outputProcessors = kafkaInput.createOutputProcessors();
            this.sc = new SinkCounter("KafkaComsumer");
        }

        public void run() {
            try {
                ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
                this.sc.start();
                while (it.hasNext()) {
                    String m = null;
                    try {
                        m = new String(it.next().message(),
                                this.kafkaInput.encoding);
                        this.sc.incrementConsumerOfKafkaCount();
                    } catch (UnsupportedEncodingException e1) {
                        this.sc.incrementConsumerOfKafkaException();
                        e1.printStackTrace();
                        logger.error(e1);
                    }

                    try {
                        Map<String, Object> event = this.decoder
                                .decode(m);

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
                        this.sc.incrementConsumerOfKafkaException();
                        logger.error("process event failed:" + m);
                        e.printStackTrace();
                        logger.error(e);
                    }

                }
            } catch (Throwable t) {
                this.sc.incrementWriteDataToEsException();
                logger.error(t);
                System.exit(1);
            }
            finally {
                this.sc.stop();
            }
        }
    }

    public Kafka(Map<String, Object> config, ArrayList<Map> filter,
                 ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
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

    public void shutdown() {

        if (consumer != null) consumer.shutdown();
        if (executor != null) executor.shutdown();
        try {
            HashMap<String, String> consumerSettings = (HashMap<String, String>) this.config
                    .get("consumer_settings");

            int timeout = 5000;
            if (consumerSettings.containsKey("auto.commit.interval.ms")) {
                timeout = Integer.parseInt(consumerSettings.get("auto.commit.interval.ms"));
            }

            if (!executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                logger.error("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted during shutdown, exiting uncleanly");
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
