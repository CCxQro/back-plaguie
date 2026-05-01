package itesm.mx.application.usecase.location.municipality;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.repository.location.MunicipalityRepository;

import java.util.List;

@ApplicationScoped
public class GetAllMunicipalitiesUseCase {

    @Inject
    MunicipalityRepository municipalityRepository;

    public List<Municipality> execute() {
        return municipalityRepository.findAllMunicipalities();
    }
}
