package com.smuehr.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class UserEventEnricher {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "user-event-enricher-app");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "streams-kafka:9092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> userPurchases = builder.stream("user-purchases");

        GlobalKTable<String, String> usersGlobalTable = builder.globalTable("user-table");

        // Enrich the stream using an inner join and write it to a kafka topic
        KStream<String, String> userPurchasesEnriched = userPurchases
                .join(
                        usersGlobalTable,
                        (key, value) -> key, // map the key of the stream to the globalTable
                        (userPurchase, userInfo) -> "Purchase=" + userPurchase + ",UserInfo=[" + userInfo + "]"
                );
        userPurchasesEnriched.to("user-purchased-enriched-inner-join");

        // Enrich the stream using a left join
        KStream<String, String> userPurchasesEnrichedLeft = userPurchases
                .leftJoin(
                        usersGlobalTable,
                        (key, value) -> key,
                        (userPurchase, userInfo) -> {
                            if (userInfo != null) {
                                return "Purchase=" + userPurchase + ",UserInfo=[" + userInfo + "]";
                            } else {
                                return "Purchase=" + userPurchase + ",UserInfo=null";
                            }
                        }
                );

        userPurchasesEnrichedLeft.to("user-purchased-enriched-left-join");

        KafkaStreams streams = new KafkaStreams(builder.build(), properties);

        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
