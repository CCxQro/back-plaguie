package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryActionConstants;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.infrastructure.mapper.marketplace.InventoryMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.InventoryEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class InventoryRepositoryImpl implements PanacheRepositoryBase<InventoryEntity, Long>, InventoryRepository {

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = InventoryMapper.toEntity(inventory);
        persistAndFlush(entity);
        // After a pure insert, the @ManyToOne 'action'/'product' fields on the managed
        // entity are still null (they're insertable=false). The caller already loaded
        // the full domain InventoryAction and Product, so we just stamp the generated id.
        inventory.setInventoryId(entity.inventoryId);
        return inventory;
    }

    @Override
    public Inventory updateCantidad(Long inventoryId, Integer cantidad) {
        InventoryEntity entity = findByIdOptional(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));
        entity.cantidad = cantidad;
        flush();
        return InventoryMapper.toDomain(entity);
    }

    @Override
    public boolean delete(Long inventoryId) {
        Optional<InventoryEntity> entity = findByIdOptional(inventoryId);
        if (entity.isEmpty()) {
            return false;
        }
        delete(entity.get());
        return true;
    }

    @Override
    public Optional<Inventory> findByInventoryId(Long inventoryId) {
        return findByIdOptional(inventoryId).map(InventoryMapper::toDomain);
    }

    @Override
    public List<Inventory> findAllBySkuSellerId(Long skuSellerId) {
        return find("skuSellerId = ?1", Sort.by("inventoryId").descending(), skuSellerId)
                .stream()
                .map(InventoryMapper::toDomain)
                .toList();
    }

    @Override
    public int sumByActionAndSkuSellerId(Long actionId, Long skuSellerId) {
        Long sum = getEntityManager().createQuery(
                        "SELECT COALESCE(SUM(i.cantidad), 0) FROM InventoryEntity i " +
                        "WHERE i.actionId = :actionId AND i.skuSellerId = :skuSellerId",
                        Long.class)
                .setParameter("actionId", actionId)
                .setParameter("skuSellerId", skuSellerId)
                .getSingleResult();
        return sum == null ? 0 : sum.intValue();
    }

    @Override
    public int currentStock(Long skuSellerId) {
        int added = sumByActionAndSkuSellerId(InventoryActionConstants.ADD, skuSellerId);
        int subtracted = sumByActionAndSkuSellerId(InventoryActionConstants.SUBTRACT, skuSellerId);
        return added - subtracted;
    }
}
