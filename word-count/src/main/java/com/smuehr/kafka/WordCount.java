package com.smuehr.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class WordCount {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "streams-kafka:9092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


        StreamsBuilder builder = new StreamsBuilder();

        // stream from kafka
        KStream<String, String> wordCountInput = builder.stream("word-count-input");

        // map values to lowercase
        KTable<String, Long> wordCounts = wordCountInput.mapValues(value -> value.toLowerCase())

                // flatmap values split by space
                .flatMapValues(value -> Arrays.asList(value.split(" ")))

                //select the new key

                .selectKey((key, value) -> value)

                // group by key
                .groupByKey()

                // count occurrences
                .count();

        // write back to kafka

        wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), properties);
        streams.start();

        // print the topology
        System.out.println(streams.toString());

//      shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
