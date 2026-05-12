package itesm.mx.application.usecase.marketplace.provider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.repository.marketplace.ProviderRepository;

@ApplicationScoped
public class RegisterProviderUseCase {

    @Inject
    ProviderRepository providerRepository;

    @Transactional
    public Provider execute(Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (provider.getName() == null || provider.getName().isBlank()) {
            throw new IllegalArgumentException("Provider name is required");
        }
        if (provider.getUser() == null || provider.getUser().getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        return providerRepository.save(provider);
    }
}