package itesm.mx.application.dto.ingestion;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for GET /api/ingestion/runs.
 */
public class IngestionRunResponseDto {
    public Long id;
    public LocalDateTime startedAt;
    public LocalDateTime finishedAt;
    public String status;
    public Integer filesFound;
    public Integer filesProcessed;
    public List<IngestionFileResponseDto> files;

    public IngestionRunResponseDto() {
    }
}
