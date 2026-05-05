package itesm.mx.infrastructure.mapper.user;

import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.persistence.entity.users.AdministratorEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class AdministratorMapper {
    public static AdministratorEntity toEntity(Administrator administrator) {
        AdministratorEntity entity = new AdministratorEntity();
        entity.administratorId = administrator.getAdministratorId();
        entity.userId = administrator.getUser().getUserId();
        entity.isActive = administrator.getActive();
        return entity;
    }

    public static Administrator toDomain(AdministratorEntity entity) {
        Administrator administrator = new Administrator();
        administrator.setAdministratorId(entity.administratorId);
        administrator.setUser(mapUser(entity));
        administrator.setActive(entity.isActive);
        return administrator;
    }

    private static User mapUser(AdministratorEntity entity) {
        UserEntity userEntity = entity.user;
        if (userEntity != null) {
            return UserMapper.toDomain(userEntity);
        }

        User user = new User();
        user.setUserId(entity.userId);
        return user;
    }
}
