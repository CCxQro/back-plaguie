package itesm.mx.infrastructure.persistence.repository.user;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import itesm.mx.infrastructure.mapper.user.TechnicalSellerMapper;
import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class TechnicalSellerRepositoryImpl implements PanacheRepositoryBase<TechnicalSellerEntity, Long>, TechnicalSellerRepository {

    @Override
    @Transactional
    public TechnicalSeller save(TechnicalSeller technicalSeller) {
        TechnicalSellerEntity entity = TechnicalSellerMapper.toEntity(technicalSeller);
        persistAndFlush(entity);
        return TechnicalSellerMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public TechnicalSeller update(TechnicalSeller technicalSeller) {
        if (technicalSeller.getTechnicalSellerId() == null) {
            throw new IllegalArgumentException("technicalSellerId is required for update");
        }

        TechnicalSellerEntity entity = findByIdOptional(technicalSeller.getTechnicalSellerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tecnico vendedor no encontrado con id: " + technicalSeller.getTechnicalSellerId()));

        if (technicalSeller.getUser() != null && technicalSeller.getUser().getUserId() != null) {
            entity.userId = technicalSeller.getUser().getUserId();
        }
        if (technicalSeller.getActive() != null) {
            entity.isActive = technicalSeller.getActive();
        }

        persistAndFlush(entity);
        return TechnicalSellerMapper.toDomain(entity);
    }

    @Override
    public Optional<TechnicalSeller> findByTechnicalSellerId(Long technicalSellerId) {
        return findByIdOptional(technicalSellerId).map(TechnicalSellerMapper::toDomain);
    }

    @Override
    public Optional<TechnicalSeller> findByIdUser(Long userId) {
        return find("userId", userId).firstResultOptional().map(TechnicalSellerMapper::toDomain);
    }

    @Override
    public List<TechnicalSeller> findAllTechnicalSellers() {
        return listAll().stream()
                .map(TechnicalSellerMapper::toDomain)
                .collect(Collectors.toList());
    }
}

