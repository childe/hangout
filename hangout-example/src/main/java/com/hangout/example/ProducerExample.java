package com.hangout.example;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;


@SpringBootApplication
public class ProducerExample {

    public static void main(String[] args) {
      SpringApplication.run(ProducerExample.class, args);


    Properties props = new Properties();
    props.put("bootstrap.servers", "kafka:9092");
//    props.put("bootstrap.servers", "172.22.0.4:9092");

    props.put("acks", "all");
    props.put("retries", 0);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    Producer<String, String> producer = new KafkaProducer<String, String>(props);


    while (true){
      String str = "{\"name\":\"test..jvm.gc.PS-MarkSweep.count\",\"@timestamp\":\"2017-04-27T09:23:43.000+0000\",\"groupKey\":\"test\",\"commandKey\":\"\",\"metricType\":\"jvm\",\"threadPool\":\"n\",\"indexType\":\"gauge\",\"value\":0}";

      ProducerRecord<String, String> data = new ProducerRecord<String, String>(
              "test-reporter",str);
      try {
        System.out.println(data);
        Future<RecordMetadata> ret = producer.send(data);
        System.out.println(ret.get().offset());
        Thread.sleep(100);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
}