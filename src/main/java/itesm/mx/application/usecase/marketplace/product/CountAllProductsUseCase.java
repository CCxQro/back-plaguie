package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.repository.marketplace.ProductRepository;

@ApplicationScoped
public class CountAllProductsUseCase {

    @Inject ProductRepository productRepository;

    public long execute() {
        return productRepository.countAllProducts();
    }
}
