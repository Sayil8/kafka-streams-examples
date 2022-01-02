package com.smuehr.kafka;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kafka docs on how to test streams: https://kafka.apache.org/28/documentation/streams/developer-guide/testing.html
 */

class WordCountTest {
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;
    private Serde<String> stringSerde = new Serdes.StringSerde();
    private Serde<Long> longSerde = new Serdes.LongSerde();

    @BeforeEach
    public void setUpTopologyTestDriver() {

        // setup test driver
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test:1234");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        WordCount wordCount = new WordCount();
        testDriver = new TopologyTestDriver(wordCount.createTopology(), config);

        // setup test topics
        inputTopic = testDriver
                .createInputTopic("word-count-input", stringSerde.serializer(), stringSerde.serializer());

        outputTopic = testDriver
                .createOutputTopic("word-count-output", stringSerde.deserializer(), longSerde.deserializer());

    }

    @AfterEach
    public void closeTestDriver() {
        testDriver.close();
    }

    public void pushNewRecord(String value) {
        inputTopic.pipeInput(null, value);
    }

    @Test
    public void ensureCountsAreCorrect() {
        String firstExample = "Testing Kafka applications";
        pushNewRecord(firstExample);
        assertEquals(new KeyValue<>("testing", 1L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("kafka", 1L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("applications", 1L), outputTopic.readKeyValue());
        assertTrue(outputTopic.isEmpty());

        String secondExample = "Testing kafka again";
        pushNewRecord(secondExample);
        assertEquals(new KeyValue<>("testing", 2L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("kafka", 2L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("again", 1L), outputTopic.readKeyValue());
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void ensureWordsBecomeLowercase() {
        String example = "Kafka KAFKA kafKa";
        pushNewRecord(example);
        assertEquals(outputTopic.readKeyValue(), new KeyValue<>("kafka", 1L));
        assertEquals(outputTopic.readKeyValue(), new KeyValue<>("kafka", 2L));
        assertEquals(outputTopic.readKeyValue(), new KeyValue<>("kafka", 3L));
        assertTrue(outputTopic.isEmpty());
    }
}