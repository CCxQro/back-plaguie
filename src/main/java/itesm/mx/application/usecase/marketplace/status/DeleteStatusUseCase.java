package itesm.mx.application.usecase.marketplace.status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.repository.marketplace.StatusRepository;

@ApplicationScoped
public class DeleteStatusUseCase {

    @Inject
    StatusRepository statusRepository;

    @Transactional
    public void execute(Long statusId) {
        if (statusId == null) {
            throw new IllegalArgumentException("Status id is required");
        }
        statusRepository.delete(statusId);
    }
}