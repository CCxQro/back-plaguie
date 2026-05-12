package itesm.mx.application.usecase.marketplace.status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.repository.marketplace.StatusRepository;

@ApplicationScoped
public class RegisterStatusUseCase {

    @Inject
    StatusRepository statusRepository;

    @Transactional
    public Status execute(Status status) {
        if (status == null || status.getName() == null || status.getName().isBlank()) {
            throw new IllegalArgumentException("Status name is required");
        }
        return statusRepository.save(status);
    }
}