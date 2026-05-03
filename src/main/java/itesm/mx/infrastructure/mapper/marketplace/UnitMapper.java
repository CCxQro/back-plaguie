package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.mapper.user.UserMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.UnitEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class UnitMapper {

    public static UnitEntity toEntity(Unit unit) {
        UnitEntity entity = new UnitEntity();
        entity.unitId = unit.getUnitId();
        entity.userId = unit.getUser().getUserId();
        entity.name = unit.getName();
        entity.statusId = unit.getStatus().getStatusId();
        return entity;
    }

    public static Unit toDomain(UnitEntity entity) {
        Unit unit = new Unit();
        unit.setUnitId(entity.unitId);
        unit.setName(entity.name);
        unit.setUser(mapUser(entity));
        unit.setStatus(mapStatus(entity));
        return unit;
    }

    private static User mapUser(UnitEntity entity) {
        UserEntity userEntity = entity.user;
        if (userEntity != null) {
            return UserMapper.toDomain(userEntity);
        }
        User user = new User();
        user.setUserId(entity.userId);
        return user;
    }

    private static Status mapStatus(UnitEntity entity) {
        StatusEntity statusEntity = entity.status;
        if (statusEntity != null) {
            return StatusMapper.toDomain(statusEntity);
        }
        Status status = new Status();
        status.setStatusId(entity.statusId);
        return status;
    }
}