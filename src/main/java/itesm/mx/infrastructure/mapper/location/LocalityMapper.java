package itesm.mx.infrastructure.mapper.location;

import itesm.mx.domain.models.location.Locality;
import itesm.mx.infrastructure.persistence.entity.location.LocalityEntity;

public class LocalityMapper {
    public static LocalityEntity toEntity(Locality locality) {
        LocalityEntity entity = new LocalityEntity();
        entity.localityId = locality.getLocalityId();
        entity.name = locality.getName();
        return entity;
    }

    public static Locality toDomain(LocalityEntity entity) {
        Locality locality = new Locality();
        locality.setLocalityId(entity.localityId);
        locality.setName(entity.name);
        return locality;
    }
}
