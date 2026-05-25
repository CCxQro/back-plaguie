package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.repository.marketplace.ProviderRepository;
import itesm.mx.infrastructure.mapper.marketplace.ProviderMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProviderEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProviderRepositoryImpl implements PanacheRepositoryBase<ProviderEntity, Long>, ProviderRepository {

    @Override
    public Provider save(Provider provider) {
        ProviderEntity entity = ProviderMapper.toEntity(provider);
        persistAndFlush(entity);
        return ProviderMapper.toDomain(entity);
    }

    @Override
    public Provider update(Long providerId, Provider provider) {
        ProviderEntity entity = findByIdOptional(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerId));
        entity.name = provider.getName();
        entity.userId = provider.getUser().getUserId();
        flush();
        return ProviderMapper.toDomain(entity);
    }

    @Override
    public boolean delete(Long providerId) {
        Optional<ProviderEntity> entity = findByIdOptional(providerId);
        if (entity.isEmpty()) {
            return false;
        }
        delete(entity.get());
        return true;
    }

    @Override
    public Optional<Provider> findByProviderId(Long providerId) {
        return findByIdOptional(providerId).map(ProviderMapper::toDomain);
    }

    @Override
    public List<Provider> findAllProviders() {
        return listAll().stream()
                .map(ProviderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Provider> findAllByUserId(Long userId) {
        return find("userId", userId).stream()
                .map(ProviderMapper::toDomain)
                .toList();
    }
}