package com.smuehr.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FavouriteColour {

    public Topology createTopology() {
        List<String> acceptedColors = Arrays.asList("yellow", "white", "black" );

        StreamsBuilder builder = new StreamsBuilder();

        // Read from kafka topic
        KStream<String, String> colorPersonInput = builder.stream("favourite-colour-input");

        // Validate input, select key
        KStream<String, String> colorPerson = colorPersonInput
                .filter(((key, value) -> value.contains(",")))
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                .mapValues(value -> value.split(",")[1].toLowerCase())
                .filter(((key, value) -> acceptedColors.contains(value)));

        /**
         * To transform a KStream into a KTable we use aggregation
         * we use a groupBy and an aggregation step
         *
         * KTables are useful when we want to have stateful operation
         * An insert of an existing key would lead to an update
         *
         * Following commented lines show the aggregation
         */
//        KTable<String, Long> colourCountByAggregation = colorPerson
//                .toTable()
//                .groupBy(((key, value) -> KeyValue.pair(value, value)))
//                .count();
//
//        colourCountByAggregation.toStream().to("favourite-colour-output", Produced.with(Serdes.String(), Serdes.Long()));


        /**
         * We want to use a KTable to handle updates to the same key
         * We transform the stream to a table by using writing to an intermediate topic
         * and reading from it as a table
         */
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

        return builder.build();
    }

    public Properties createProperties() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app-colour");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "streams-kafka:9092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return properties;
    }

    public static void main(String[] args) {

        FavouriteColour favouriteColour = new FavouriteColour();
        KafkaStreams streams = new KafkaStreams(favouriteColour.createTopology(), favouriteColour.createProperties());

        // Don't use in prod
        streams.cleanUp();

        streams.start();

        //shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
