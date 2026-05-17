package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class CountNormalStockProductsUseCase {

    private static final int NORMAL_STOCK_THRESHOLD = 30;

    @Inject ProductRepository productRepository;

    public long execute() {
        return productRepository.countProductsByStockAbove(NORMAL_STOCK_THRESHOLD);
    }
}
