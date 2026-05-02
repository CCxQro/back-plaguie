package itesm.mx.application.usecase.marketplace.status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.repository.marketplace.StatusRepository;

@ApplicationScoped
public class UpdateStatusUseCase {

    @Inject
    StatusRepository statusRepository;

    @Transactional
    public Status execute(Long statusId, Status status) {
        if (statusId == null) {
            throw new IllegalArgumentException("Status id is required");
        }
        if (status == null || status.getName() == null || status.getName().isBlank()) {
            throw new IllegalArgumentException("Status name is required");
        }
        return statusRepository.update(statusId, status);
    }
}