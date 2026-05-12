package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;

import java.util.List;

@ApplicationScoped
public class GetProductsBySellerUseCase {

    @Inject ProductRepository productRepository;
    @Inject TechnicalSellerRepository technicalSellerRepository;

    public List<Product> execute(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId is required");
        }
        technicalSellerRepository.findByTechnicalSellerId(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
        return productRepository.findAllBySellerId(sellerId);
    }
}
