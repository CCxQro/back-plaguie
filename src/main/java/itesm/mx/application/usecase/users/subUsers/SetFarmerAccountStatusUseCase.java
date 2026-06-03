package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.user.FarmerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Sets the account approval status (Accepted / Revision / Rejected) of a farmer
 * identified by its user id. Used by the administrator to approve or reject
 * self-registered farmer accounts (HU-23).
 */
@ApplicationScoped
public class SetFarmerAccountStatusUseCase {

    @Inject
    FarmerRepository farmerRepository;

    @Transactional
    public Farmer execute(Long userId, Long statusId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }
        if (statusId == null) {
            throw new IllegalArgumentException("Se requiere el estado de la cuenta");
        }

        Farmer farmer = farmerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Agricultor no encontrado para el usuario con id: " + userId));

        farmer.setStatusId(statusId);
        return farmerRepository.update(farmer);
    }
}
