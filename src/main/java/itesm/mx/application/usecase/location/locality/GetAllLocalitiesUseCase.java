package itesm.mx.application.usecase.location.locality;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.repository.location.LocalityRepository;

import java.util.List;

@ApplicationScoped
public class GetAllLocalitiesUseCase {

    @Inject
    LocalityRepository localityRepository;

    public List<Locality> execute() {
        return localityRepository.findAllLocalities();
    }
}
