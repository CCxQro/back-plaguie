package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;

import java.util.List;

@ApplicationScoped
public class GetProductsBySellerUseCase {

    @Inject ProductRepository productRepository;
    @Inject TechnicalSellerRepository technicalSellerRepository;
    @Inject PriceRepository priceRepository;
    @Inject InventoryRepository inventoryRepository;

    public List<Product> execute(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId is required");
        }
        technicalSellerRepository.findByTechnicalSellerId(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
        List<Product> products = productRepository.findAllBySellerId(sellerId);
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
