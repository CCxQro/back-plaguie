package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Status;

import java.util.List;
import java.util.Optional;

public interface StatusRepository {
    List<Status> findAllStatuses();
    Status save(Status status);
    Optional<Status> findByStatusId(Long statusId);
    Status update(Long statusId, Status status);
    void delete(Long statusId);
}