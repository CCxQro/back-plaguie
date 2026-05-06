package itesm.mx.application.usecase.marketplace.provider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.repository.marketplace.ProviderRepository;

@ApplicationScoped
public class DeleteProviderUseCase {

    @Inject
    ProviderRepository providerRepository;

    @Transactional
    public boolean execute(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("Provider id is required");
        }
        return providerRepository.delete(providerId);
    }
}