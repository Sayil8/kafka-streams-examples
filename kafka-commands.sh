#!/bin/bash

### Run the kafka container and exec into it

# create input topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic word-count-input

# create output topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic word-count-output

# create compact topic
kafka-topics --create --topic intermediate-favourite-colour --partitions 2 --replication-factor 1 --if-not-exists \
--zookeeper  0.0.0.0:2181 --config cleanup.policy=compact

# start a kafka producer
kafka-console-producer --broker-list localhost:9092 --topic word-count-input


# verify the data has been written
kafka-console-consumer --bootstrap-server localhost:9092 --topic word-count-input --from-beginning

# start a consumer on the output topic
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic bank-transactions-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
