package itesm.mx.application.usecase.marketplace.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.repository.marketplace.InventoryActionRepository;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class GetActionSummaryUseCase {

    @Inject InventoryRepository inventoryRepository;
    @Inject InventoryActionRepository inventoryActionRepository;
    @Inject ProductRepository productRepository;

    public int execute(Long actionId, Long skuSellerId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId is required");
        }
        if (skuSellerId == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        inventoryActionRepository.findByInventoryActionId(actionId)
                .orElseThrow(() -> new IllegalArgumentException("InventoryAction not found: " + actionId));
        productRepository.findByProductId(skuSellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + skuSellerId));
        return inventoryRepository.sumByActionAndSkuSellerId(actionId, skuSellerId);
    }
}
