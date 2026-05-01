package itesm.mx.domain.repository.location;

import itesm.mx.domain.models.location.Municipality;

import java.util.List;
import java.util.Optional;

public interface MunicipalityRepository {
    List<Municipality> findAllMunicipalities();
    Municipality register(Municipality municipality);
    Optional<Municipality> findByName(String name);
}
