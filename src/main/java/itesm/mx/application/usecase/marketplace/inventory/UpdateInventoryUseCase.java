package itesm.mx.application.usecase.marketplace.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryActionConstants;
import itesm.mx.domain.repository.marketplace.InventoryRepository;

@ApplicationScoped
public class UpdateInventoryUseCase {

    @Inject InventoryRepository inventoryRepository;

    @Transactional
    public Inventory execute(Long inventoryId, Integer newCantidad) {
        if (inventoryId == null) {
            throw new IllegalArgumentException("inventoryId is required");
        }
        if (newCantidad == null || newCantidad <= 0) {
            throw new IllegalArgumentException("cantidad must be greater than 0");
        }

        Inventory existing = inventoryRepository.findByInventoryId(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        Long skuSellerId = existing.getProduct().getSkuSellerId();
        Long actionId = existing.getAction().getInventoryActionId();
        int currentStock = inventoryRepository.currentStock(skuSellerId);

        int oldContribution = signedContribution(actionId, existing.getCantidad());
        int newContribution = signedContribution(actionId, newCantidad);
        int hypothetical = currentStock - oldContribution + newContribution;
        if (hypothetical < 0) {
            throw new IllegalArgumentException(
                    "Operation would result in negative stock (resulting=" + hypothetical + ")");
        }

        return inventoryRepository.updateCantidad(inventoryId, newCantidad);
    }

    private int signedContribution(Long actionId, Integer cantidad) {
        if (InventoryActionConstants.ADD.equals(actionId)) {
            return cantidad;
        }
        return -cantidad;
    }
}
