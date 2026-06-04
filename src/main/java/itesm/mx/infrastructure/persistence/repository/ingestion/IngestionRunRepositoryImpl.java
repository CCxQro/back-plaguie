package itesm.mx.infrastructure.persistence.repository.ingestion;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.models.ingestion.IngestionRun;
import itesm.mx.domain.repository.ingestion.IngestionRunRepository;
import itesm.mx.infrastructure.persistence.entity.ingestion.IngestionRunEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IngestionRunRepositoryImpl
        implements PanacheRepositoryBase<IngestionRunEntity, Long>, IngestionRunRepository {

    @Override
    public IngestionRun save(IngestionRun run) {
        IngestionRunEntity entity = toEntity(run);
        persistAndFlush(entity);
        run.setId(entity.id);
        return run;
    }

    @Override
    public IngestionRun update(IngestionRun run) {
        IngestionRunEntity entity = findByIdOptional(run.getId())
                .orElseThrow(() -> new IllegalArgumentException("IngestionRun not found: " + run.getId()));
        entity.finishedAt = run.getFinishedAt();
        entity.status = run.getStatus();
        entity.filesFound = run.getFilesFound();
        entity.filesProcessed = run.getFilesProcessed();
        flush();
        return run;
    }

    @Override
    public Optional<IngestionRun> findRunById(Long id) {
        return findByIdOptional(id).map(this::toDomain);
    }

    @Override
    public List<IngestionRun> findAllOrderedByStartedAtDesc() {
        return find("ORDER BY startedAt DESC").list().stream()
                .map(this::toDomain)
                .toList();
    }

    private IngestionRunEntity toEntity(IngestionRun run) {
        IngestionRunEntity e = new IngestionRunEntity();
        e.id = run.getId();
        e.startedAt = run.getStartedAt();
        e.finishedAt = run.getFinishedAt();
        e.status = run.getStatus();
        e.filesFound = run.getFilesFound();
        e.filesProcessed = run.getFilesProcessed();
        return e;
    }

    private IngestionRun toDomain(IngestionRunEntity e) {
        IngestionRun run = new IngestionRun();
        run.setId(e.id);
        run.setStartedAt(e.startedAt);
        run.setFinishedAt(e.finishedAt);
        run.setStatus(e.status);
        run.setFilesFound(e.filesFound);
        run.setFilesProcessed(e.filesProcessed);
        if (e.files != null) {
            run.setFiles(e.files.stream().map(f -> {
                IngestionFile file = new IngestionFile();
                file.setId(f.id);
                file.setRunId(f.runId);
                file.setSourceUrl(f.sourceUrl);
                file.setFilename(f.filename);
                file.setEtag(f.etag);
                file.setLastModified(f.lastModified);
                file.setContentLength(f.contentLength);
                file.setChecksum(f.checksum);
                file.setStatus(f.status);
                file.setRowsTotal(f.rowsTotal);
                file.setRowsInserted(f.rowsInserted);
                file.setRowsSkipped(f.rowsSkipped);
                file.setError(f.error);
                file.setCreatedAt(f.createdAt);
                return file;
            }).toList());
        }
        return run;
    }
}
