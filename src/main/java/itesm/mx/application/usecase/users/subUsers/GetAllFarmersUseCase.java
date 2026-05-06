package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.user.FarmerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetAllFarmersUseCase {

    @Inject
    FarmerRepository farmerRepository;

    public List<Farmer> execute() {
        return farmerRepository.findAllFarmers();
    }
}

