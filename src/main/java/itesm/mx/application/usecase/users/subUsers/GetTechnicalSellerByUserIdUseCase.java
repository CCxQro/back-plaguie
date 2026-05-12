package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class GetTechnicalSellerByUserIdUseCase {

    @Inject
    TechnicalSellerRepository technicalSellerRepository;

    public Optional<TechnicalSeller> execute(Long userId) {
        return technicalSellerRepository.findByIdUser(userId);
    }
}