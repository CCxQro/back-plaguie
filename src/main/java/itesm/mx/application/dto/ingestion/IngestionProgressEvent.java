package itesm.mx.application.dto.ingestion;

/**
 * Kafka message sent to topic ingestion.progress.
 * Also exposed as SSE payload via GET /api/ingestion/progress/stream.
 */
public class IngestionProgressEvent {
    public Long runId;
    public String filename;
    public Integer rowsProcessed;
    public Integer totalRows;
    public String status; // DOWNLOADING, PARSING, INSERTING, DONE, FAILED

    public IngestionProgressEvent() {
    }

    public IngestionProgressEvent(Long runId, String filename, Integer rowsProcessed, Integer totalRows, String status) {
        this.runId = runId;
        this.filename = filename;
        this.rowsProcessed = rowsProcessed;
        this.totalRows = totalRows;
        this.status = status;
    }
}
