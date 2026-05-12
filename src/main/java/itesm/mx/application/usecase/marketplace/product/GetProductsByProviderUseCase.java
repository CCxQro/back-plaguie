package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.marketplace.ProviderRepository;

import java.util.List;

@ApplicationScoped
public class GetProductsByProviderUseCase {

    @Inject ProductRepository productRepository;
    @Inject ProviderRepository providerRepository;

    public List<Product> execute(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("providerId is required");
        }
        providerRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        return productRepository.findAllByProviderId(providerId);
    }
}
