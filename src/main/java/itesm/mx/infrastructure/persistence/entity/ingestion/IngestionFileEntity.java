package itesm.mx.infrastructure.persistence.entity.ingestion;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingestion_file", indexes = {
        @Index(name = "idx_ingestion_file_source_url", columnList = "source_url")
})
public class IngestionFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "run_id")
    public Long runId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", referencedColumnName = "id", insertable = false, updatable = false)
    public IngestionRunEntity run;

    @Column(name = "source_url", nullable = false, length = 512)
    public String sourceUrl;

    @Column(name = "filename", length = 255)
    public String filename;

    @Column(name = "etag", length = 255)
    public String etag;

    @Column(name = "last_modified", length = 100)
    public String lastModified;

    @Column(name = "content_length")
    public Long contentLength;

    @Column(name = "checksum", length = 64) // SHA-256 hex = 64 chars
    public String checksum;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "rows_total")
    public Integer rowsTotal;

    @Column(name = "rows_inserted")
    public Integer rowsInserted;

    @Column(name = "rows_skipped")
    public Integer rowsSkipped;

    @Column(name = "error", columnDefinition = "TEXT")
    public String error;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}
