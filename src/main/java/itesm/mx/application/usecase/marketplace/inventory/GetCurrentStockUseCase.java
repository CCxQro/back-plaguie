package itesm.mx.application.usecase.marketplace.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class GetCurrentStockUseCase {

    @Inject InventoryRepository inventoryRepository;
    @Inject ProductRepository productRepository;

    public int execute(Long skuSellerId) {
        if (skuSellerId == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        productRepository.findByProductId(skuSellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + skuSellerId));
        return inventoryRepository.currentStock(skuSellerId);
    }
}
