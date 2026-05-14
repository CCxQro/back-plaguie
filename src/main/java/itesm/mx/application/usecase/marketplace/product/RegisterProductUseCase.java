package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.CategoryRepository;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.marketplace.ProviderRepository;
import itesm.mx.domain.repository.marketplace.StatusRepository;
import itesm.mx.domain.repository.marketplace.UnitRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApplicationScoped
public class RegisterProductUseCase {

    @Inject ProductRepository productRepository;
    @Inject TechnicalSellerRepository technicalSellerRepository;
    @Inject CategoryRepository categoryRepository;
    @Inject ProviderRepository providerRepository;
    @Inject UnitRepository unitRepository;
    @Inject StatusRepository statusRepository;
    @Inject PriceRepository priceRepository;

    @Transactional
    public Product execute(Product product) {
        if (product.getSkuSellerId() == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new IllegalArgumentException("sku is required");
        }
        if (product.getUnitValue() == null || product.getUnitValue() <= 0) {
            throw new IllegalArgumentException("unitValue must be greater than 0");
        }
        if (product.getSeller() == null || product.getSeller().getTechnicalSellerId() == null) {
            throw new IllegalArgumentException("seller is required");
        }
        if (product.getCategory() == null || product.getCategory().getCategoryId() == null) {
            throw new IllegalArgumentException("category is required");
        }
        if (product.getProvider() == null || product.getProvider().getProviderId() == null) {
            throw new IllegalArgumentException("provider is required");
        }
        if (product.getUnit() == null || product.getUnit().getUnitId() == null) {
            throw new IllegalArgumentException("unit is required");
        }
        if (product.getStatus() == null || product.getStatus().getStatusId() == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (product.getLatestPrice() == null || product.getLatestPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("price must be greater than 0");
        }

        technicalSellerRepository.findByTechnicalSellerId(product.getSeller().getTechnicalSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
        categoryRepository.findByCategoryId(product.getCategory().getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        providerRepository.findByProviderId(product.getProvider().getProviderId())
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        unitRepository.findByUnitId(product.getUnit().getUnitId())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found"));
        statusRepository.findByStatusId(product.getStatus().getStatusId())
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));

        Product saved = productRepository.save(product);

        Price priceRow = new Price();
        priceRow.setProduct(saved);
        priceRow.setPrice(product.getLatestPrice());
        priceRow.setPriceDate(LocalDateTime.now());
        Price persistedPrice = priceRepository.save(priceRow);

        saved.setLatestPrice(persistedPrice.getPrice());
        saved.setLatestPriceDate(persistedPrice.getPriceDate());
        return saved;
    }
}
