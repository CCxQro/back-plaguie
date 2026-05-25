package itesm.mx.infrastructure.mapper.location;

import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.models.location.State;
import itesm.mx.infrastructure.persistence.entity.location.LocalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.location.MunicipalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.PropertyEntity;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;

public class LocationMapper {
    public static LocationEntity toEntity(Location location) {
        LocationEntity entity = new LocationEntity();
        entity.locationId = location.getLocationId();
        entity.coordinates = location.getCoordinates();
        entity.stateId = location.getState().getStateId();
        entity.municipalityId = location.getMunicipality().getMunicipalityId();
        entity.localityId = location.getLocality().getLocalityId();
        entity.propertyId = location.getProperty().getPropertyId();
        return entity;
    }

    public static Location toDomain(LocationEntity entity) {
        Location location = new Location();
        location.setLocationId(entity.locationId);
        location.setCoordinates(entity.coordinates);
        location.setState(mapState(entity));
        location.setMunicipality(mapMunicipality(entity));
        location.setLocality(mapLocality(entity));
        location.setProperty(mapProperty(entity));
        return location;
    }

    private static State mapState(LocationEntity entity) {
        StateEntity stateEntity = entity.state;
        if (stateEntity != null) {
            return StateMapper.toDomain(stateEntity);
        }

        return new State(entity.stateId, null);
    }

    private static Municipality mapMunicipality(LocationEntity entity) {
        MunicipalityEntity municipalityEntity = entity.municipality;
        if (municipalityEntity != null) {
            return MunicipalityMapper.toDomain(municipalityEntity);
        }

        return new Municipality(entity.municipalityId, null);
    }

    private static Locality mapLocality(LocationEntity entity) {
        LocalityEntity localityEntity = entity.locality;
        if (localityEntity != null) {
            return LocalityMapper.toDomain(localityEntity);
        }

        return new Locality(entity.localityId, null);
    }

    private static Property mapProperty(LocationEntity entity) {
        PropertyEntity propertyEntity = entity.property;
        if (propertyEntity != null) {
            return PropertyMapper.toDomain(propertyEntity);
        }

        return new Property(entity.propertyId, null);
    }
}
