package itesm.mx.application.usecase.location.locality;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.repository.location.LocalityRepository;
import itesm.mx.domain.util.LocationNormalizer;

@ApplicationScoped
public class RegisterLocalityUseCase {

    @Inject
    LocalityRepository localityRepository;

    @Transactional
    public Locality execute(Locality locality) {
        if (locality == null) {
            throw new IllegalArgumentException("La localidad es requerida");
        }

        String normalizedName = LocationNormalizer.normalize(locality.getName());

        return localityRepository.findByName(normalizedName)
                .orElseGet(() -> localityRepository.register(
                        new Locality(locality.getLocalityId(), normalizedName)
                ));
    }
}
