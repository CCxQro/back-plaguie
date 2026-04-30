package itesm.mx.application.usecase.location.property;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.repository.location.PropertyRepository;
import itesm.mx.domain.util.LocationNormalizer;

@ApplicationScoped
public class RegisterPropertyUseCase {

    @Inject
    PropertyRepository propertyRepository;

    @Transactional
    public Property execute(Property property) {
        if (property == null) {
            throw new IllegalArgumentException("El predio es requerido");
        }

        String normalizedName = LocationNormalizer.normalize(property.getName());

        return propertyRepository.findByName(normalizedName)
                .orElseGet(() -> propertyRepository.register(
                        new Property(property.getPropertyId(), normalizedName)
                ));
    }
}
