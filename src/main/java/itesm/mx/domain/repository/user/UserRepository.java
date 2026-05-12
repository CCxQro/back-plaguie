package itesm.mx.domain.repository.user;

import itesm.mx.domain.models.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByFirebaseUuid(String firebaseUuid);
    Optional<User> findUserById(Long userId);
    List<User> findAllUsers();
    List<User> findUsersFiltered(int page, int size, String name, Integer roleId, Boolean isActive);
    long countUsersFiltered(String name, Integer roleId, Boolean isActive);
    User save(User user);
    User update(User user);
    void deactivate(Long userId);
}
