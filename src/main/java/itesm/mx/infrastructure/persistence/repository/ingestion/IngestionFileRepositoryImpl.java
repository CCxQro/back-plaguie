package itesm.mx.infrastructure.persistence.repository.ingestion;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.repository.ingestion.IngestionFileRepository;
import itesm.mx.infrastructure.persistence.entity.ingestion.IngestionFileEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IngestionFileRepositoryImpl
        implements PanacheRepositoryBase<IngestionFileEntity, Long>, IngestionFileRepository {

    @Override
    public IngestionFile save(IngestionFile file) {
        IngestionFileEntity entity = toEntity(file);
        persistAndFlush(entity);
        file.setId(entity.id);
        return file;
    }

    @Override
    public IngestionFile update(IngestionFile file) {
        IngestionFileEntity entity = findByIdOptional(file.getId())
                .orElseThrow(() -> new IllegalArgumentException("IngestionFile not found: " + file.getId()));
        entity.etag = file.getEtag();
        entity.lastModified = file.getLastModified();
        entity.contentLength = file.getContentLength();
        entity.checksum = file.getChecksum();
        entity.status = file.getStatus();
        entity.rowsTotal = file.getRowsTotal();
        entity.rowsInserted = file.getRowsInserted();
        entity.rowsSkipped = file.getRowsSkipped();
        entity.error = file.getError();
        flush();
        return file;
    }

    @Override
    public Optional<IngestionFile> findBySourceUrl(String sourceUrl) {
        return find("sourceUrl = ?1 ORDER BY id DESC", sourceUrl).firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public Optional<IngestionFile> findByChecksum(String checksum) {
        return find("checksum = ?1 ORDER BY id DESC", checksum).firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public List<IngestionFile> findByRunId(Long runId) {
        return find("runId = ?1", runId).list().stream()
                .map(this::toDomain)
                .toList();
    }

    private IngestionFileEntity toEntity(IngestionFile file) {
        IngestionFileEntity e = new IngestionFileEntity();
        e.id = file.getId();
        e.runId = file.getRunId();
        e.sourceUrl = file.getSourceUrl();
        e.filename = file.getFilename();
        e.etag = file.getEtag();
        e.lastModified = file.getLastModified();
        e.contentLength = file.getContentLength();
        e.checksum = file.getChecksum();
        e.status = file.getStatus();
        e.rowsTotal = file.getRowsTotal();
        e.rowsInserted = file.getRowsInserted();
        e.rowsSkipped = file.getRowsSkipped();
        e.error = file.getError();
        e.createdAt = file.getCreatedAt();
        return e;
    }

    private IngestionFile toDomain(IngestionFileEntity e) {
        IngestionFile file = new IngestionFile();
        file.setId(e.id);
        file.setRunId(e.runId);
        file.setSourceUrl(e.sourceUrl);
        file.setFilename(e.filename);
        file.setEtag(e.etag);
        file.setLastModified(e.lastModified);
        file.setContentLength(e.contentLength);
        file.setChecksum(e.checksum);
        file.setStatus(e.status);
        file.setRowsTotal(e.rowsTotal);
        file.setRowsInserted(e.rowsInserted);
        file.setRowsSkipped(e.rowsSkipped);
        file.setError(e.error);
        file.setCreatedAt(e.createdAt);
        return file;
    }
}
