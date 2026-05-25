package itesm.mx.application.mapper.location;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.RegisterLocationDto;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.LocationData;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public final class LocationDtoMapper {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private LocationDtoMapper() {
    }

    public static LocationData toLocationData(RegisterLocationDto dto) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.longitude, dto.latitude));
        point.setSRID(4326);

        return new LocationData(
                point,
                dto.stateName,
                dto.municipalityName,
                dto.localityName,
                dto.propertyName
        );
    }

    public static GetLocationResponseDto toResponseDto(Location location) {
        Double latitude = location.getCoordinates() != null ? location.getCoordinates().getY() : null;
        Double longitude = location.getCoordinates() != null ? location.getCoordinates().getX() : null;

        return new GetLocationResponseDto(
                location.getLocationId(),
                latitude,
                longitude,
                location.getState() != null ? location.getState().getStateId() : null,
                location.getState() != null ? location.getState().getName() : null,
                location.getMunicipality() != null ? location.getMunicipality().getMunicipalityId() : null,
                location.getMunicipality() != null ? location.getMunicipality().getName() : null,
                location.getLocality() != null ? location.getLocality().getLocalityId() : null,
                location.getLocality() != null ? location.getLocality().getName() : null,
                location.getProperty() != null ? location.getProperty().getPropertyId() : null,
                location.getProperty() != null ? location.getProperty().getName() : null
        );
    }
}
