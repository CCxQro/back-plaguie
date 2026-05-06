package itesm.mx.application.usecase.marketplace.status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.repository.marketplace.StatusRepository;

import java.util.List;

@ApplicationScoped
public class GetAllStatusesUseCase {

    @Inject
    StatusRepository statusRepository;

    public List<Status> execute() {
        return statusRepository.findAllStatuses();
    }
}