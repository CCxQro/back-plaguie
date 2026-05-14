package itesm.mx.application.usecase.marketplace.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryAction;
import itesm.mx.domain.models.marketplace.InventoryActionConstants;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.InventoryActionRepository;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class RegisterInventoryUseCase {

    @Inject InventoryRepository inventoryRepository;
    @Inject InventoryActionRepository inventoryActionRepository;
    @Inject ProductRepository productRepository;

    @Transactional
    public Inventory execute(Inventory inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("inventory is required");
        }
        if (inventory.getProduct() == null || inventory.getProduct().getSkuSellerId() == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        if (inventory.getAction() == null || inventory.getAction().getInventoryActionId() == null) {
            throw new IllegalArgumentException("actionId is required");
        }
        if (inventory.getCantidad() == null || inventory.getCantidad() <= 0) {
            throw new IllegalArgumentException("cantidad must be greater than 0");
        }

        Long skuSellerId = inventory.getProduct().getSkuSellerId();
        Product product = productRepository.findByProductId(skuSellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + skuSellerId));

        Long actionId = inventory.getAction().getInventoryActionId();
        InventoryAction action = inventoryActionRepository.findByInventoryActionId(actionId)
                .orElseThrow(() -> new IllegalArgumentException("InventoryAction not found: " + actionId));

        if (InventoryActionConstants.SUBTRACT.equals(actionId)) {
            int currentStock = inventoryRepository.currentStock(skuSellerId);
            if (currentStock - inventory.getCantidad() < 0) {
                throw new IllegalArgumentException(
                        "Operation would result in negative stock (current=" + currentStock
                                + ", subtract=" + inventory.getCantidad() + ")");
            }
        }

        inventory.setProduct(product);
        inventory.setAction(action);
        return inventoryRepository.save(inventory);
    }
}
