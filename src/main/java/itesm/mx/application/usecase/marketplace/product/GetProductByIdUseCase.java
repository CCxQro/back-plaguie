package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class GetProductByIdUseCase {

    @Inject ProductRepository productRepository;
    @Inject PriceRepository priceRepository;

    public Product execute(Long skuSellerId) {
        if (skuSellerId == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        Product product = productRepository.findByProductId(skuSellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        priceRepository.findLatestBySkuSellerId(skuSellerId).ifPresent(latest -> {
            product.setLatestPrice(latest.getPrice());
            product.setLatestPriceDate(latest.getPriceDate());
        });
        return product;
    }
}
