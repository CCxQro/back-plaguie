package itesm.mx.infrastructure.mapper.user;

import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.models.location.Location;
import itesm.mx.infrastructure.mapper.location.LocationMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class TechnicalSellerMapper {
    public static TechnicalSellerEntity toEntity(TechnicalSeller technicalSeller) {
        TechnicalSellerEntity entity = new TechnicalSellerEntity();
        entity.technicalSellerId = technicalSeller.getTechnicalSellerId();
        entity.userId = technicalSeller.getUser().getUserId();
        entity.locationId = technicalSeller.getLocation().getLocationId();
        entity.isActive = technicalSeller.getActive();
        return entity;
    }

    public static TechnicalSeller toDomain(TechnicalSellerEntity entity) {
        TechnicalSeller technicalSeller = new TechnicalSeller();
        technicalSeller.setTechnicalSellerId(entity.technicalSellerId);
        technicalSeller.setUser(mapUser(entity));
        technicalSeller.setLocation(mapLocation(entity));
        technicalSeller.setActive(entity.isActive);
        return technicalSeller;
    }

    private static User mapUser(TechnicalSellerEntity entity) {
        UserEntity userEntity = entity.user;
        if (userEntity != null) {
            return UserMapper.toDomain(userEntity);
        }

        User user = new User();
        user.setUserId(entity.userId);
        return user;
    }

    private static Location mapLocation(TechnicalSellerEntity entity) {
        LocationEntity locationEntity = entity.location;
        if (locationEntity != null) {
            return LocationMapper.toDomain(locationEntity);
        }

        Location location = new Location();
        location.setLocationId(entity.locationId);
        return location;
    }
}
