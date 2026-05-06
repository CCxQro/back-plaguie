package itesm.mx.application.usecase.marketplace.provider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.repository.marketplace.ProviderRepository;

import java.util.List;

@ApplicationScoped
public class GetProvidersByUserUseCase {

    @Inject
    ProviderRepository providerRepository;

    public List<Provider> execute(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return providerRepository.findAllByUserId(userId);
    }
}