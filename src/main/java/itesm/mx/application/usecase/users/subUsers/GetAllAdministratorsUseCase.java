package itesm.mx.application.usecase.users;

import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.repository.AdministratorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetAllAdministratorsUseCase {

    @Inject
    AdministratorRepository administratorRepository;

    public List<Administrator> execute() {
        return administratorRepository.findAllAdministrators();
    }
}

