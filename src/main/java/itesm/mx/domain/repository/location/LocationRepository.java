package itesm.mx.domain.repository.location;

import itesm.mx.domain.models.location.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    List<Location> findAllLocations();

    Optional<Location> findLocationById(Long locationId);

    Location register(Location location);

    Location update(Location location);
}
