package com.smuehr.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BankBalanceProducerTest {

    @Test
    public void getTransactionTest() {
        ProducerRecord<String, String> record = BankBalanceProducer.getTransaction("Test");
        String key = record.key();
        String value = record.value();

        assertEquals(key, "Test");

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(value);
            assertEquals(node.get("name").asText(), "Test");
            assertTrue(node.get("amount").asInt() < 100, "Amount less than 100");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(value);
    }
}
