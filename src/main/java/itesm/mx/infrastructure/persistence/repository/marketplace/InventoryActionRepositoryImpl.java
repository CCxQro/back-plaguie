package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.InventoryAction;
import itesm.mx.domain.repository.marketplace.InventoryActionRepository;
import itesm.mx.infrastructure.mapper.marketplace.InventoryActionMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.InventoryActionEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class InventoryActionRepositoryImpl implements PanacheRepositoryBase<InventoryActionEntity, Long>, InventoryActionRepository {

    @Override
    public Optional<InventoryAction> findByInventoryActionId(Long inventoryActionId) {
        return findByIdOptional(inventoryActionId).map(InventoryActionMapper::toDomain);
    }

    @Override
    public List<InventoryAction> findAllActions() {
        return listAll().stream()
                .map(InventoryActionMapper::toDomain)
                .toList();
    }
}
