package itesm.mx.infrastructure.ingestion.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import itesm.mx.application.dto.ingestion.IngestionProgressEvent;

public class IngestionProgressEventDeserializer extends ObjectMapperDeserializer<IngestionProgressEvent> {
    public IngestionProgressEventDeserializer() {
        super(IngestionProgressEvent.class);
    }
}
