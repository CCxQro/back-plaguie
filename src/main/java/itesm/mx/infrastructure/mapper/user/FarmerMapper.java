package itesm.mx.infrastructure.mapper.user;

import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.models.location.Location;
import itesm.mx.infrastructure.mapper.location.LocationMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class FarmerMapper {
    public static FarmerEntity toEntity(Farmer farmer) {
        FarmerEntity entity = new FarmerEntity();
        entity.farmerId = farmer.getFarmerId();
        entity.userId = farmer.getUser().getUserId();
        entity.locationId = farmer.getLocation().getLocationId();
        entity.isActive = farmer.getActive();
        return entity;
    }

    public static Farmer toDomain(FarmerEntity entity) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(entity.farmerId);
        farmer.setUser(mapUser(entity));
        farmer.setLocation(mapLocation(entity));
        farmer.setActive(entity.isActive);
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

    private static Location mapLocation(FarmerEntity entity) {
        LocationEntity locationEntity = entity.location;
        if (locationEntity != null) {
            return LocationMapper.toDomain(locationEntity);
        }

        Location location = new Location();
        location.setLocationId(entity.locationId);
        return location;
    }
}
