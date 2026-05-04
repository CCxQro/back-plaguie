package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetAllTechnicalSellersUseCase {

    @Inject
    TechnicalSellerRepository technicalSellerRepository;

    public List<TechnicalSeller> execute() {
        return technicalSellerRepository.findAllTechnicalSellers();
    }
}

