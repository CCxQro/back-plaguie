package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.repository.user.AdministratorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class GetAdministratorByUserIdUseCase {

    @Inject
    AdministratorRepository administratorRepository;

    public Optional<Administrator> execute(Long userId) {
        return administratorRepository.findByIdUser(userId);
    }
}