package itesm.mx.domain.repository.location;

import itesm.mx.domain.models.location.Property;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository {
    List<Property> findAllProperties();
    Property register(Property property);
    Optional<Property> findByName(String name);
}
