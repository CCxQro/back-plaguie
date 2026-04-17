package itesm.mx.infrastructure.mapper;

import itesm.mx.domain.models.User;
import itesm.mx.infrastructure.persistence.entity.UserEntity;

public class UserMapper {
    public static UserEntity toEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.userId = user.getUserId();
        userEntity.firebaseUuid = user.getFirebaseUuid();
        userEntity.name = user.getName();
        userEntity.email = user.getEmail();
        userEntity.password = user.getPassword();
        userEntity.roleId = user.getRoleId();
        return userEntity;
    }

    public static User toDomain(UserEntity userEntity) {
        return new User(
            userEntity.userId,
            userEntity.firebaseUuid,
            userEntity.name,
            userEntity.email,
            userEntity.password,
            userEntity.roleId
        );
    }
    
}
