package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.mapper.user.UserMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProviderEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class ProviderMapper {

    public static ProviderEntity toEntity(Provider provider) {
        ProviderEntity entity = new ProviderEntity();
        entity.providerId = provider.getProviderId();
        entity.userId = provider.getUser().getUserId();
        entity.name = provider.getName();
        return entity;
    }

    public static Provider toDomain(ProviderEntity entity) {
        Provider provider = new Provider();
        provider.setProviderId(entity.providerId);
        provider.setName(entity.name);
        provider.setUser(mapUser(entity));
        return provider;
    }

    private static User mapUser(ProviderEntity entity) {
        UserEntity userEntity = entity.user;
        if (userEntity != null) {
            return UserMapper.toDomain(userEntity);
        }
        User user = new User();
        user.setUserId(entity.userId);
        return user;
    }
}