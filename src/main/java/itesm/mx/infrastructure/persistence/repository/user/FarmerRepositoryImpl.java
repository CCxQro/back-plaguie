package itesm.mx.infrastructure.persistence.repository.user;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.infrastructure.mapper.user.FarmerMapper;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class FarmerRepositoryImpl implements PanacheRepositoryBase<FarmerEntity, Long>, FarmerRepository {

    @Override
    @Transactional
    public Farmer save(Farmer farmer) {
        FarmerEntity entity = FarmerMapper.toEntity(farmer);
        persistAndFlush(entity);
        return FarmerMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Farmer update(Farmer farmer) {
        if (farmer.getFarmerId() == null) {
            throw new IllegalArgumentException("farmerId is required for update");
        }

        FarmerEntity entity = findByIdOptional(farmer.getFarmerId())
                .orElseThrow(() -> new IllegalArgumentException("Agricultor no encontrado con id: " + farmer.getFarmerId()));

        if (farmer.getUser() != null && farmer.getUser().getUserId() != null) {
            entity.userId = farmer.getUser().getUserId();
        }
        if (farmer.getActive() != null) {
            entity.isActive = farmer.getActive();
        }

        persistAndFlush(entity);
        return FarmerMapper.toDomain(entity);
    }

    @Override
    public Optional<Farmer> findByFarmerId(Long farmerId) {
        return findByIdOptional(farmerId).map(FarmerMapper::toDomain);
    }

    @Override
    public Optional<Farmer> findByIdUser(Long userId) {
        return find("userId", userId).firstResultOptional().map(FarmerMapper::toDomain);
    }

    @Override
    public List<Farmer> findAllFarmers() {
        return listAll().stream()
                .map(FarmerMapper::toDomain)
                .collect(Collectors.toList());
    }
}

