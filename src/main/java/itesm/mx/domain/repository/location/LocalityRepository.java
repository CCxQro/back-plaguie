package itesm.mx.domain.repository.location;

import itesm.mx.domain.models.location.Locality;

import java.util.List;
import java.util.Optional;

public interface LocalityRepository {
    List<Locality> findAllLocalities();
    Locality register(Locality locality);
    Optional<Locality> findByName(String name);
}
