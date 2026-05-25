package itesm.mx.application.usecase.marketplace.price;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApplicationScoped
public class RegisterPriceUseCase {

    @Inject PriceRepository priceRepository;
    @Inject ProductRepository productRepository;

    @Transactional
    public Price execute(Price price) {
        if (price == null) {
            throw new IllegalArgumentException("price is required");
        }
        if (price.getProduct() == null || price.getProduct().getSkuSellerId() == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        if (price.getPrice() == null || price.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("price must be greater than 0");
        }

        Long skuSellerId = price.getProduct().getSkuSellerId();
        Product product = productRepository.findByProductId(skuSellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + skuSellerId));

        price.setProduct(product);
        price.setPriceDate(LocalDateTime.now());
        return priceRepository.save(price);
    }
}