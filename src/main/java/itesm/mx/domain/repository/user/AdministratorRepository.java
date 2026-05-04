package itesm.mx.domain.repository.user;

import itesm.mx.domain.models.user.Administrator;

import java.util.List;
import java.util.Optional;

public interface AdministratorRepository {
    Administrator save(Administrator administrator);
    Administrator update(Administrator administrator);
    Optional<Administrator> findByAdministratorId(Long administratorId);
    Optional<Administrator> findByIdUser(Long userId);
    List<Administrator> findAllAdministrators();
}

