package itesm.mx.infrastructure.persistence.repository.user;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.UserRepository;
import itesm.mx.infrastructure.mapper.user.UserMapper;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Optional<User> findUserById(Long userId) {
        return findByIdOptional(userId).map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAllUsers() {
        return listAll().stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = UserMapper.toEntity(user);
        persistAndFlush(userEntity);
        return UserMapper.toDomain(userEntity);
    }

    @Override
    @Transactional
    public User update(User user) {
        UserEntity entity = findByIdOptional(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + user.getUserId()));

        if (user.getName() != null && !user.getName().isBlank()) {
            entity.name = user.getName();
        }
        if (user.getRoleId() != null) {
            entity.roleId = user.getRoleId();
        }
        if (user.getActive() != null) {
            entity.isActive = user.getActive();
        }

        persistAndFlush(entity);
        return UserMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deactivate(Long userId) {
        UserEntity entity = findByIdOptional(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + userId));

        entity.isActive = false;
        persistAndFlush(entity);
    }
}
