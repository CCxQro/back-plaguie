package itesm.mx.infrastructure.mapper.user;

import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class FarmerMapper {
    public static FarmerEntity toEntity(Farmer farmer) {
        FarmerEntity entity = new FarmerEntity();
        entity.farmerId = farmer.getFarmerId();
        entity.userId = farmer.getUser().getUserId();
        entity.isActive = farmer.getActive();
        entity.statusId = farmer.getStatusId();
        return entity;
    }

    public static Farmer toDomain(FarmerEntity entity) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(entity.farmerId);
        farmer.setUser(mapUser(entity));
        farmer.setActive(entity.isActive);
        farmer.setStatusId(entity.statusId);
        if (entity.status != null) {
            farmer.setStatusName(entity.status.name);
        }
        return farmer;
    }

    private static User mapUser(FarmerEntity entity) {
        UserEntity userEntity = entity.user;
        if (userEntity != null) {
            return UserMapper.toDomain(userEntity);
        }

        User user = new User();
        user.setUserId(entity.userId);
        return user;
    }
}
