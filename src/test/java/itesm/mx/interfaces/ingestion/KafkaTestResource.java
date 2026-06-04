package itesm.mx.interfaces.ingestion;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Switches Kafka channels to in-memory for @QuarkusTest.
 * Allows ingestion pipeline integration tests without a running Kafka broker.
 */
public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        // Outgoing channels
        env.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory("ingestion-files-out"));
        env.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory("ingestion-progress-out"));
        // Incoming channels
        env.putAll(InMemoryConnector.switchIncomingChannelsToInMemory("ingestion-files-in"));
        env.putAll(InMemoryConnector.switchIncomingChannelsToInMemory("ingestion-progress-in"));
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
