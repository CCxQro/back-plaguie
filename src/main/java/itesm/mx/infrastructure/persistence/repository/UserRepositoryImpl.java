package itesm.mx.infrastructure.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;
import itesm.mx.infrastructure.mapper.UserMapper;
import itesm.mx.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

@ApplicationScoped
public class UserRepositoryImpl implements PanacheRepositoryBase<UserEntity, Long>, UserRepository {

    @Override
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional().map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByFirebaseUuid(String firebaseUuid) {
        return find("firebaseUuid", firebaseUuid).firstResultOptional().map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = UserMapper.toEntity(user);
        persistAndFlush(userEntity);
        return UserMapper.toDomain(userEntity);
    }
}