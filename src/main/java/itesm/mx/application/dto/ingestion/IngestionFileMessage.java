package itesm.mx.application.dto.ingestion;

/**
 * Kafka message sent to topic ingestion.files.
 * Produced by CheckIngestionUseCase, consumed by IngestionWorker.
 */
public class IngestionFileMessage {
    public Long runId;
    public String sourceUrl;
    public String filename;
    public String etag;
    public String checksum;

    public IngestionFileMessage() {
    }

    public IngestionFileMessage(Long runId, String sourceUrl, String filename, String etag, String checksum) {
        this.runId = runId;
        this.sourceUrl = sourceUrl;
        this.filename = filename;
        this.etag = etag;
        this.checksum = checksum;
    }
}
