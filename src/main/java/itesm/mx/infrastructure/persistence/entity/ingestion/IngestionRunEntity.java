package itesm.mx.infrastructure.persistence.entity.ingestion;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ingestion_run")
public class IngestionRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "started_at", nullable = false)
    public LocalDateTime startedAt;

    @Column(name = "finished_at")
    public LocalDateTime finishedAt;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "files_found")
    public Integer filesFound;

    @Column(name = "files_processed")
    public Integer filesProcessed;

    @OneToMany(mappedBy = "run", fetch = FetchType.LAZY)
    public List<IngestionFileEntity> files;
}
