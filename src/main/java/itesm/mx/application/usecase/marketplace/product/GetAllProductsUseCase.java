package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;

import java.util.List;

@ApplicationScoped
public class GetAllProductsUseCase {

    @Inject ProductRepository productRepository;
    @Inject PriceRepository priceRepository;
    @Inject InventoryRepository inventoryRepository;

    public List<Product> execute() {
        List<Product> products = productRepository.findAllProducts();
        products.forEach(p -> {
            priceRepository.findLatestBySkuSellerId(p.getSkuSellerId())
                    .ifPresent(latest -> {
                        p.setLatestPrice(latest.getPrice());
                        p.setLatestPriceDate(latest.getPriceDate());
                    });
            p.setStock(inventoryRepository.currentStock(p.getSkuSellerId()));
        });
        return products;
    }
}
