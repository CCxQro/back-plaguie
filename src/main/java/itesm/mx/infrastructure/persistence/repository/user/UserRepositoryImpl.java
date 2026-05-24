package itesm.mx.infrastructure.persistence.repository.user;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.UserRepository;
import itesm.mx.infrastructure.mapper.user.UserMapper;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<User> findUsersFiltered(int page, int size, String name, Integer roleId, Boolean isActive) {
        if (hasNoFilters(name, roleId, isActive)) {
            return findAll().page(page, size).list().stream()
                    .map(UserMapper::toDomain).collect(Collectors.toList());
        }
        return find(buildFilterQuery(name, roleId, isActive), buildFilterParams(name, roleId, isActive))
                .page(page, size).list().stream()
                .map(UserMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countUsersFiltered(String name, Integer roleId, Boolean isActive) {
        if (hasNoFilters(name, roleId, isActive)) {
            return count();
        }
        return find(buildFilterQuery(name, roleId, isActive), buildFilterParams(name, roleId, isActive)).count();
    }

    private boolean hasNoFilters(String name, Integer roleId, Boolean isActive) {
        return (name == null || name.isBlank()) && roleId == null && isActive == null;
    }

    private String buildFilterQuery(String name, Integer roleId, Boolean isActive) {
        List<String> conditions = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            conditions.add("(lower(name) like :name or lower(email) like :name)");
        }
        if (roleId != null) {
            conditions.add("roleId = :roleId");
        }
        if (isActive != null) {
            conditions.add("isActive = :isActive");
        }
        return String.join(" and ", conditions);
    }

    private Map<String, Object> buildFilterParams(String name, Integer roleId, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        if (name != null && !name.isBlank()) {
            params.put("name", "%" + name.toLowerCase() + "%");
        }
        if (roleId != null) {
            params.put("roleId", roleId);
        }
        if (isActive != null) {
            params.put("isActive", isActive);
        }
        return params;
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
        if (user.getLocation() != null && user.getLocation().getLocationId() != null) {
            entity.locationId = user.getLocation().getLocationId();
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
