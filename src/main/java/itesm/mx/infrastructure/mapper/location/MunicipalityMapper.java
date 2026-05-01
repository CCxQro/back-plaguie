package itesm.mx.infrastructure.mapper.location;

import itesm.mx.domain.models.location.Municipality;
import itesm.mx.infrastructure.persistence.entity.location.MunicipalityEntity;

public class MunicipalityMapper {
    public static MunicipalityEntity toEntity(Municipality municipality) {
        MunicipalityEntity entity = new MunicipalityEntity();
        entity.municipalityId = municipality.getMunicipalityId();
        entity.name = municipality.getName();
        return entity;
    }

    public static Municipality toDomain(MunicipalityEntity entity) {
        Municipality municipality = new Municipality();
        municipality.setMunicipalityId(entity.municipalityId);
        municipality.setName(entity.name);
        return municipality;
    }
}
