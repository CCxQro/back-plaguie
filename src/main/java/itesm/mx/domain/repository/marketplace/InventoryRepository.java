package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Inventory save(Inventory inventory);
    Inventory updateCantidad(Long inventoryId, Integer cantidad);
    boolean delete(Long inventoryId);
    Optional<Inventory> findByInventoryId(Long inventoryId);
    List<Inventory> findAllBySkuSellerId(Long skuSellerId);
    int sumByActionAndSkuSellerId(Long actionId, Long skuSellerId);
    int currentStock(Long skuSellerId);
}
