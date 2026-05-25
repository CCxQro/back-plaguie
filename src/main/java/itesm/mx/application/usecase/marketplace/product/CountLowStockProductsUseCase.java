package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class CountLowStockProductsUseCase {

    private static final int LOW_STOCK_MIN = 5;
    private static final int LOW_STOCK_MAX = 30;

    @Inject ProductRepository productRepository;

    public long execute() {
        return productRepository.countProductsByStockBetween(LOW_STOCK_MIN, LOW_STOCK_MAX);
    }
}
