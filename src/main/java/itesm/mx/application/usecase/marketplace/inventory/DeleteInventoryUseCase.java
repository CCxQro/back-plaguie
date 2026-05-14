package itesm.mx.application.usecase.marketplace.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryActionConstants;
import itesm.mx.domain.repository.marketplace.InventoryRepository;

@ApplicationScoped
public class DeleteInventoryUseCase {

    @Inject InventoryRepository inventoryRepository;

    @Transactional
    public void execute(Long inventoryId) {
        if (inventoryId == null) {
            throw new IllegalArgumentException("inventoryId is required");
        }

        Inventory existing = inventoryRepository.findByInventoryId(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        Long skuSellerId = existing.getProduct().getSkuSellerId();
        Long actionId = existing.getAction().getInventoryActionId();
        int currentStock = inventoryRepository.currentStock(skuSellerId);

        int contribution = InventoryActionConstants.ADD.equals(actionId)
                ? existing.getCantidad()
                : -existing.getCantidad();
        int hypothetical = currentStock - contribution;
        if (hypothetical < 0) {
            throw new IllegalArgumentException(
                    "Removing this row would result in negative stock (resulting=" + hypothetical + ")");
        }

        inventoryRepository.delete(inventoryId);
    }
}
