package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class DeleteProductUseCase {

    @Inject ProductRepository productRepository;

    @Transactional
    public boolean execute(Long skuSellerId) {
        if (skuSellerId == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        boolean deleted = productRepository.delete(skuSellerId);
        if (!deleted) {
            throw new IllegalArgumentException("Product not found");
        }
        return true;
    }
}
