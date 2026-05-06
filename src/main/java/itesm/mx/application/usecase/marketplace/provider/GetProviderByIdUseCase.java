package itesm.mx.application.usecase.marketplace.provider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.repository.marketplace.ProviderRepository;

import java.util.Optional;

@ApplicationScoped
public class GetProviderByIdUseCase {

    @Inject
    ProviderRepository providerRepository;

    public Optional<Provider> execute(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("Provider id is required");
        }
        return providerRepository.findByProviderId(providerId);
    }
}