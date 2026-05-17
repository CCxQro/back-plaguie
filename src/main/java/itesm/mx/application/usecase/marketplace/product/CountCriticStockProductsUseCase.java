package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class CountCriticStockProductsUseCase {

    private static final int CRITIC_STOCK_THRESHOLD = 5;

    @Inject ProductRepository productRepository;

    public long execute() {
        return productRepository.countProductsByStockBelow(CRITIC_STOCK_THRESHOLD);
    }
}
