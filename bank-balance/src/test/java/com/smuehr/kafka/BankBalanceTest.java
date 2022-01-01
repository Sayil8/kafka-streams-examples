package com.smuehr.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BankBalanceTest {

    @Test
    public void newBalanceTest() {

        // create the current balance
        ObjectNode balance = JsonNodeFactory.instance.objectNode();
        balance.put("count", 20);
        balance.put("balance", 2000);
        balance.put("time", Instant.now().toString());

        // create the new transaction
        ObjectNode newTransaction = JsonNodeFactory.instance.objectNode();
        newTransaction.put("amount", 1000);
        newTransaction.put("time", Instant.now().toString());


        JsonNode result = BankBalance.newBalance(newTransaction, balance);

        assertEquals(result.get("balance").asInt(), 3000);
        assertEquals(result.get("count").asInt(), 21);
    }
}