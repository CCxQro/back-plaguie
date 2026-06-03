package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.AccountStatusConstants;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.user.FarmerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Returns the farmer accounts that are pending administrator approval
 * (status = Revision). Used to populate the admin approval queue (HU-23).
 */
@ApplicationScoped
public class GetPendingFarmersUseCase {

    @Inject
    FarmerRepository farmerRepository;

    public List<Farmer> execute() {
        return farmerRepository.findByStatus(AccountStatusConstants.REVISION);
    }
}
