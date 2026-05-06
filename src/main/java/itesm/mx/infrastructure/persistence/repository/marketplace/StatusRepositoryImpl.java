package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.repository.marketplace.StatusRepository;
import itesm.mx.infrastructure.mapper.marketplace.StatusMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StatusRepositoryImpl implements PanacheRepositoryBase<StatusEntity, Long>, StatusRepository {

    @Override
    public List<Status> findAllStatuses() {
        return listAll().stream()
                .map(StatusMapper::toDomain)
                .toList();
    }

    @Override
    public Status save(Status status) {
        StatusEntity entity = StatusMapper.toEntity(status);
        persistAndFlush(entity);
        return StatusMapper.toDomain(entity);
    }

    @Override
    public Optional<Status> findByStatusId(Long statusId) {
        return findByIdOptional(statusId).map(StatusMapper::toDomain);
    }

    @Override
    public Status update(Long statusId, Status status) {
        StatusEntity entity = findByIdOptional(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status not found: " + statusId));
        entity.name = status.getName();
        flush();
        return StatusMapper.toDomain(entity);
    }

    @Override
    public void delete(Long statusId) {
        StatusEntity entity = findByIdOptional(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status not found: " + statusId));
        delete(entity);
    }
}