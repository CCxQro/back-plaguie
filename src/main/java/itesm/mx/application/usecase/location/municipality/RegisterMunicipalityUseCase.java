package itesm.mx.application.usecase.location.municipality;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.repository.location.MunicipalityRepository;
import itesm.mx.domain.util.LocationNormalizer;

@ApplicationScoped
public class RegisterMunicipalityUseCase {

    @Inject
    MunicipalityRepository municipalityRepository;

    @Transactional
    public Municipality execute(Municipality municipality) {
        if (municipality == null) {
            throw new IllegalArgumentException("El municipio es requerido");
        }

        String normalizedName = LocationNormalizer.normalize(municipality.getName());

        return municipalityRepository.findByName(normalizedName)
                .orElseGet(() -> municipalityRepository.register(
                        new Municipality(municipality.getMunicipalityId(), normalizedName)
                ));
    }
}
