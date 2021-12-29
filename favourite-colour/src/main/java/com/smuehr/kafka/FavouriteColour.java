package com.smuehr.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FavouriteColour {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app-colour");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "streams-kafka:9092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        List<String> acceptedColors = Arrays.asList("green", "red", "blue" );

        StreamsBuilder builder = new StreamsBuilder();

        // Read from kafka topic
        KStream<String, String> colorPersonInput = builder.stream("favourite-colour-input");

        // Validate input, select key
        KStream<String, String> colorPerson = colorPersonInput
                .filter(((key, value) -> value.contains(",")))
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                .mapValues(value -> value.split(",")[1].toLowerCase())
                .filter(((key, value) -> acceptedColors.contains(value)));

        // Write to an intermediate kafka topic
        colorPerson.to("intermediate-favourite-colour", Produced.with(Serdes.String(), Serdes.String()));


        // Read the topic as a KTable
        KTable<String, String> colorPersonTable = builder.table("intermediate-favourite-colour");

        // Group and count the number of colors
        KTable<String, Long> colourCount = colorPersonTable
                .groupBy(((key, value) -> KeyValue.pair(value,value)))
                .count();

        // Write the results back to kafka
        colourCount.toStream().to("favourite-colour-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), properties);

        // Dont use in prod
        streams.cleanUp();

        streams.start();

        //shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
