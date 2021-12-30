package com.smuehr.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class BankBalanceProducer {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "streams-kafka:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Producer acks
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, "3");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, "1");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        // Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        while (true) {
            System.out.println("Producing batch");
            try {
                producer.send(getTransaction("Michael"));
                Thread.sleep(100);
                producer.send(getTransaction("Marianne"));
                Thread.sleep(100);
                producer.send(getTransaction("Marcus"));
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        producer.close();
    }

    public static ProducerRecord<String, String> getTransaction(String name) {
        JSONObject transaction = new JSONObject();
        Integer amount = ThreadLocalRandom.current().nextInt(0, 100);
        transaction.put("name", name);
        transaction.put("amount", amount);
        transaction.put("time", Instant.now().toString());
        return new ProducerRecord<String, String>("bank-transaction", name, transaction.toString());
    }
}
