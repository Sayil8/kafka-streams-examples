package com.smuehr.kafka;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FavouriteColourTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;
    private TestOutputTopic<String, String> intermediateTopic;
    private Serde<String> stringSerde = new Serdes.StringSerde();
    private Serde<Long> longSerde = new Serdes.LongSerde();

    @BeforeEach
    void setUp() {
        FavouriteColour favouriteColour = new FavouriteColour();
        Properties config = favouriteColour.createProperties();
        testDriver = new TopologyTestDriver(favouriteColour.createTopology(), config);

        // setup test topics
        inputTopic = testDriver
                .createInputTopic("favourite-colour-input", stringSerde.serializer(), stringSerde.serializer());

        outputTopic = testDriver
                .createOutputTopic("favourite-colour-output", stringSerde.deserializer(), longSerde.deserializer());

        intermediateTopic = testDriver
                .createOutputTopic("intermediate-favourite-colour", stringSerde.deserializer(), stringSerde.deserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    public void ensureFavouriteColoursCount() {
        String firstPerson = "Test1,yellow";
        String secondPerson = "Test1,black";
        String thirdPerson = "Test1,yellow";

        // Push messages to the input topic
        inputTopic.pipeInput(null, firstPerson);
        inputTopic.pipeInput(null, secondPerson);
        inputTopic.pipeInput(null, thirdPerson);

        // Read from the output topic and assert results
        assertEquals(new KeyValue<>("yellow", 1L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("yellow", 0L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("black", 1L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("black", 0L), outputTopic.readKeyValue());
        assertEquals(new KeyValue<>("yellow", 1L), outputTopic.readKeyValue());
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void ensureIntermediateTopicHasCorrectResults() {
        String firstPerson = "Test1,yellow";
        String secondPerson = "Test1,black";
        String thirdPerson = "Test1,yellow";

        // Push messages to the input topic
        inputTopic.pipeInput(null, firstPerson);
        inputTopic.pipeInput(null, secondPerson);
        inputTopic.pipeInput(null, thirdPerson);

        // Read from the output topic and assert results
        assertEquals(new KeyValue<>("test1", "yellow"), intermediateTopic.readKeyValue());
        assertEquals(new KeyValue<>("test1", "black"), intermediateTopic.readKeyValue());
        assertEquals(new KeyValue<>("test1", "yellow"), intermediateTopic.readKeyValue());
        assertTrue(intermediateTopic.isEmpty());
    }

    @Test
    public void ensureOnlyAcceptedColorsAreUsed() {
        String firstPerson = "Test1,black";
        String secondPerson = "Test1,pink";

        // Push messages
        inputTopic.pipeInput(null, firstPerson);
        inputTopic.pipeInput(null, secondPerson);

        // Consume output
        assertEquals(new KeyValue<>("black", 1L), outputTopic.readKeyValue());
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void ensureOnlyValidMessagesAreProcessed() {
        String firstPerson = "Test1 black";

        // Push messages
        inputTopic.pipeInput(null, firstPerson);

        // Consume output
        assertTrue(outputTopic.isEmpty());
    }
}