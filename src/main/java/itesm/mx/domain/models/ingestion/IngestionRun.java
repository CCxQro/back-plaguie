package itesm.mx.domain.models.ingestion;

import java.time.LocalDateTime;
import java.util.List;

public class IngestionRun {
    private Long id;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status; // RUNNING, COMPLETED, FAILED
    private Integer filesFound;
    private Integer filesProcessed;
    private List<IngestionFile> files;

    public IngestionRun() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getFilesFound() { return filesFound; }
    public void setFilesFound(Integer filesFound) { this.filesFound = filesFound; }

    public Integer getFilesProcessed() { return filesProcessed; }
    public void setFilesProcessed(Integer filesProcessed) { this.filesProcessed = filesProcessed; }

    public List<IngestionFile> getFiles() { return files; }
    public void setFiles(List<IngestionFile> files) { this.files = files; }
}
