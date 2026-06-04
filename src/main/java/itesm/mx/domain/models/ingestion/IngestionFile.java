package itesm.mx.domain.models.ingestion;

import java.time.LocalDateTime;

public class IngestionFile {
    private Long id;
    private Long runId;
    private String sourceUrl;
    private String filename;
    private String etag;
    private String lastModified;
    private Long contentLength;
    private String checksum; // SHA-256
    private String status;   // PENDING, DOWNLOADING, PARSING, INSERTING, DONE, FAILED, SKIPPED
    private Integer rowsTotal;
    private Integer rowsInserted;
    private Integer rowsSkipped;
    private String error;
    private LocalDateTime createdAt;

    public IngestionFile() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRunId() { return runId; }
    public void setRunId(Long runId) { this.runId = runId; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }

    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }

    public Long getContentLength() { return contentLength; }
    public void setContentLength(Long contentLength) { this.contentLength = contentLength; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRowsTotal() { return rowsTotal; }
    public void setRowsTotal(Integer rowsTotal) { this.rowsTotal = rowsTotal; }

    public Integer getRowsInserted() { return rowsInserted; }
    public void setRowsInserted(Integer rowsInserted) { this.rowsInserted = rowsInserted; }

    public Integer getRowsSkipped() { return rowsSkipped; }
    public void setRowsSkipped(Integer rowsSkipped) { this.rowsSkipped = rowsSkipped; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
