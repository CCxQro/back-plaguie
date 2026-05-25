package itesm.mx.application.usecase.location.property;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.repository.location.PropertyRepository;

import java.util.List;

@ApplicationScoped
public class GetAllPropertiesUseCase {

    @Inject
    PropertyRepository propertyRepository;

    public List<Property> execute() {
        return propertyRepository.findAllProperties();
    }
}
