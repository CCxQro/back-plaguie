package itesm.mx.domain.repository.location;

import itesm.mx.domain.models.location.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    List<Location> findAllLocations();

    Optional<Location> findByResolvedData(
            Double x,
            Double y,
            Long stateId,
            Long municipalityId,
            Long localityId,
            Long propertyId
    );

    Location register(Location location);
}
