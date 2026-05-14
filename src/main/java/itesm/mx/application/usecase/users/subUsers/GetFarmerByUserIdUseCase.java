package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.user.FarmerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class GetFarmerByUserIdUseCase {

    @Inject
    FarmerRepository farmerRepository;

    public Optional<Farmer> execute(Long userId) {
        return farmerRepository.findByIdUser(userId);
    }
}