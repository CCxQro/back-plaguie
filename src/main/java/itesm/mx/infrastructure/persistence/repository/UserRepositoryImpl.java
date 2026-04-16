package itesm.mx.infrastructure.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;
import itesm.mx.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

@ApplicationScoped
public class UserRepositoryImpl implements PanacheRepositoryBase<UserEntity, Long>, UserRepository {

    @Override
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional().map(this::toDomain);
    }

    @Override
    public Optional<User> findByFirebaseUuid(String firebaseUuid) {
        return find("firebaseUuid", firebaseUuid).firstResultOptional().map(this::toDomain);
    }

    private User toDomain(UserEntity entity) {
        return new User(
            entity.userId,
            entity.firebaseUuid,
            entity.name,
                entity.email,
            entity.password,
            entity.roleId
        );
    }
}