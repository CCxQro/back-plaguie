package itesm.mx.application.dto.ingestion;

import java.time.LocalDateTime;

/**
 * Response DTO for individual file within GET /api/ingestion/runs.
 */
public class IngestionFileResponseDto {
    public Long id;
    public String sourceUrl;
    public String filename;
    public String status;
    public Integer rowsTotal;
    public Integer rowsInserted;
    public Integer rowsSkipped;
    public String error;
    public LocalDateTime createdAt;

    public IngestionFileResponseDto() {
    }
}
