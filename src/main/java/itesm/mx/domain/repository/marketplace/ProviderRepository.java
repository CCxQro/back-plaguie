package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Provider;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository {
    Provider save(Provider provider);
    Provider update(Long providerId, Provider provider);
    boolean delete(Long providerId);
    Optional<Provider> findByProviderId(Long providerId);
    List<Provider> findAllProviders();
    List<Provider> findAllByUserId(Long userId);
}