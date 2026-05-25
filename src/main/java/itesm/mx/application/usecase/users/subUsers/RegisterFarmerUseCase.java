package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.user.FarmerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegisterFarmerUseCase {

    @Inject
    FarmerRepository farmerRepository;

    @Transactional
    public Farmer execute(Farmer farmer) {
        if (farmer == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (farmer.getUser() == null || farmer.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Se requiere el id_usuario del agricultor");
        }

        return farmerRepository.save(farmer);
    }
}
