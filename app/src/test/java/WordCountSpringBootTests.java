package dev.responsive.quickstart;

import dev.responsive.kafka.api.ResponsiveTopologyTestDriver;
import dev.responsive.kafka.api.config.ResponsiveConfig;
import dev.responsive.kafka.api.config.StorageBackend;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordCountSpringBootTests {

    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    private ResponsiveTopologyTestDriver driver;

    private Properties getTestStreamProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-application-id");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ResponsiveConfig.STORAGE_BACKEND_TYPE_CONFIG, StorageBackend.MONGO_DB.name());
        props.put(ResponsiveConfig.MONGO_CONNECTION_STRING_CONFIG, "mongodb://localhost:27017");
        props.put(ResponsiveConfig.ASYNC_THREAD_POOL_SIZE_CONFIG, 2);
        return props;
    }
    @BeforeEach
    public void setup() {
        final Properties streamProperties = getTestStreamProperties();

        final WordCountSpringBoot stream = new WordCountSpringBoot();
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        stream.buildWordCountTopology(streamsBuilder);

        Topology topology = streamsBuilder.build();
        driver = new ResponsiveTopologyTestDriver(topology, streamProperties, Instant.EPOCH);

        String INPUT_TOPIC = "words";
        String OUTPUT_TOPIC = "output";
        inputTopic = driver.createInputTopic(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        outputTopic = driver.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.Long().deserializer());
    }

    @AfterEach
    public void tearDown() {
        driver.close();

    }

    @Test
    public void countWords() {
        String words = "the quick brown fox jumps over the lazy dog";
        inputTopic.pipeInput("test", words);
        List<KeyValue<String,Long>> produced = outputTopic.readKeyValuesToList();
        ArrayList<KeyValue<String,Long>> expected = new ArrayList<>();
        expected.add(new KeyValue<>("the", 1L));
        expected.add(new KeyValue<>("quick", 1L));
        expected.add(new KeyValue<>("brown", 1L));
        expected.add(new KeyValue<>("fox", 1L));
        expected.add(new KeyValue<>("jumps", 1L));
        expected.add(new KeyValue<>("over", 1L));
        expected.add(new KeyValue<>("the", 2L));
        expected.add(new KeyValue<>("lazy", 1L));
        expected.add(new KeyValue<>("dog", 1L));
        produced.forEach(item -> assertEquals(expected.removeFirst(), item));
        assertTrue(expected.isEmpty());
    }
}
