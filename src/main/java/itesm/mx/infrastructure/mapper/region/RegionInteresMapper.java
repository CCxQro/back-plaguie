package itesm.mx.infrastructure.mapper.region;

import itesm.mx.domain.models.region.RegionInteres;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;
import itesm.mx.infrastructure.persistence.entity.region.RegionInteresEntity;

public final class RegionInteresMapper {

    private RegionInteresMapper() {
    }

    public static RegionInteresEntity toEntity(RegionInteres domain) {
        RegionInteresEntity entity = new RegionInteresEntity();
        entity.regionInteresId = domain.getRegionInteresId();
        entity.userId = domain.getUserId();
        entity.stateId = domain.getStateId();
        entity.createdAt = domain.getCreatedAt();
        return entity;
    }

    public static RegionInteres toDomain(RegionInteresEntity entity) {
        RegionInteres domain = new RegionInteres();
        domain.setRegionInteresId(entity.regionInteresId);
        domain.setUserId(entity.userId);
        domain.setStateId(entity.stateId);
        domain.setStateName(mapStateName(entity));
        domain.setCreatedAt(entity.createdAt);
        return domain;
    }

    private static String mapStateName(RegionInteresEntity entity) {
        StateEntity state = entity.state;
        return state != null ? state.name : null;
    }
}
