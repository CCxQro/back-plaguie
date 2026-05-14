package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.InventoryAction;

import java.util.List;
import java.util.Optional;

public interface InventoryActionRepository {
    Optional<InventoryAction> findByInventoryActionId(Long inventoryActionId);
    List<InventoryAction> findAllActions();
}
