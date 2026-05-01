package itesm.mx.application.usecase.users;

import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.repository.AdministratorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegisterAdministratorUseCase {

    @Inject
    AdministratorRepository administratorRepository;

    @Transactional
    public Administrator execute(Administrator administrator) {
        if (administrator == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (administrator.getUser() == null || administrator.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Se requiere el id_usuario del administrador");
        }

        return administratorRepository.save(administrator);
    }
}

