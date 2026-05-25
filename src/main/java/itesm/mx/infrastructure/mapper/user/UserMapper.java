package itesm.mx.infrastructure.mapper.user;

import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.mapper.location.LocationMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class UserMapper {
    public static UserEntity toEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.userId = user.getUserId();
        userEntity.firebaseUuid = user.getFirebaseUuid();
        userEntity.name = user.getName();
        userEntity.email = user.getEmail();
        userEntity.roleId = user.getRoleId();
        userEntity.isActive = user.getActive();
        if (user.getLocation() != null) {
            userEntity.locationId = user.getLocation().getLocationId();
        }
        return userEntity;
    }

    public static User toDomain(UserEntity userEntity) {
        User user = new User();
        user.setUserId(userEntity.userId);
        user.setFirebaseUuid(userEntity.firebaseUuid);
        user.setName(userEntity.name);
        user.setEmail(userEntity.email);
        user.setRoleId(userEntity.roleId);
        user.setActive(userEntity.isActive);
        user.setLocation(mapLocation(userEntity));
        return user;
    }

    private static Location mapLocation(UserEntity entity) {
        LocationEntity locationEntity = entity.location;
        if (locationEntity != null) {
            return LocationMapper.toDomain(locationEntity);
        }
        if (entity.locationId != null) {
            Location location = new Location();
            location.setLocationId(entity.locationId);
            return location;
        }
        return null;
    }
}
