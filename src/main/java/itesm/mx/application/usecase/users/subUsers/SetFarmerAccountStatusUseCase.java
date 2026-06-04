package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.AccountStatusConstants;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Sets the account approval status (Accepted / Revision / Rejected) of a farmer
 * identified by its user id. Used by the administrator to approve or reject
 * self-registered farmer accounts (HU-23).
 *
 * <p>Side-effects on approval/rejection:</p>
 * <ul>
 *   <li>Accepted (1) → FarmerEntity.isActive = true,  UserEntity.isActive = true</li>
 *   <li>Rejected (3) → FarmerEntity.isActive = false, UserEntity.isActive = false</li>
 *   <li>Revision (2) → isActive flags left unchanged</li>
 * </ul>
 * This ensures the admin users table always reflects the real account state and
 * the login gate works correctly for both the farmer status check and the
 * user-level isActive check.
 */
@ApplicationScoped
public class SetFarmerAccountStatusUseCase {

    @Inject
    FarmerRepository farmerRepository;

    @Inject
    UserRepository userRepository;

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

        // Sync isActive on both the farmer sub-record and the base user record
        // so that the admin table and the login gate stay consistent.
        if (AccountStatusConstants.ACCEPTED.equals(statusId) || AccountStatusConstants.REJECTED.equals(statusId)) {
            boolean active = AccountStatusConstants.ACCEPTED.equals(statusId);
            farmer.setActive(active);

            User user = userRepository.findUserById(userId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Usuario no encontrado con id: " + userId));
            user.setActive(active);
            userRepository.update(user);
        }

        return farmerRepository.update(farmer);
    }
}
