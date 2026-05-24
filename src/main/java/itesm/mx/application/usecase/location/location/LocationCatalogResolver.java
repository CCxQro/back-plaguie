package itesm.mx.application.usecase.location.location;

import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.repository.location.LocalityRepository;
import itesm.mx.domain.repository.location.MunicipalityRepository;
import itesm.mx.domain.repository.location.PropertyRepository;
import itesm.mx.domain.repository.location.StateRepository;
import itesm.mx.domain.util.LocationNormalizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LocationCatalogResolver {

    @Inject
    StateRepository stateRepository;

    @Inject
    MunicipalityRepository municipalityRepository;

    @Inject
    LocalityRepository localityRepository;

    @Inject
    PropertyRepository propertyRepository;

    public State resolveState(String stateName) {
        String normalizedName = LocationNormalizer.normalize(stateName);
        return stateRepository.findByName(normalizedName)
                .orElseGet(() -> stateRepository.register(new State(null, normalizedName)));
    }

    public Municipality resolveMunicipality(String municipalityName) {
        String normalizedName = LocationNormalizer.normalize(municipalityName);
        return municipalityRepository.findByName(normalizedName)
                .orElseGet(() -> municipalityRepository.register(new Municipality(null, normalizedName)));
    }

    public Locality resolveLocality(String localityName) {
        String normalizedName = LocationNormalizer.normalize(localityName);
        return localityRepository.findByName(normalizedName)
                .orElseGet(() -> localityRepository.register(new Locality(null, normalizedName)));
    }

    public Property resolveProperty(String propertyName) {
        String normalizedName = LocationNormalizer.normalize(propertyName);
        return propertyRepository.findByName(normalizedName)
                .orElseGet(() -> propertyRepository.register(new Property(null, normalizedName)));
    }
}
