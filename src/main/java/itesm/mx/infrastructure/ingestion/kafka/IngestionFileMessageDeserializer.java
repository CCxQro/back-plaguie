package itesm.mx.infrastructure.ingestion.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import itesm.mx.application.dto.ingestion.IngestionFileMessage;

public class IngestionFileMessageDeserializer extends ObjectMapperDeserializer<IngestionFileMessage> {
    public IngestionFileMessageDeserializer() {
        super(IngestionFileMessage.class);
    }
}
