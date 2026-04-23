package itesm.mx.domain.repository;

import itesm.mx.domain.models.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByFirebaseUuid(String firebaseUuid);
    User save(User user);
}