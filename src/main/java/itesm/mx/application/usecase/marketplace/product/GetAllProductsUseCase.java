package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.ProductRepository;

import java.util.List;

@ApplicationScoped
public class GetAllProductsUseCase {

    @Inject ProductRepository productRepository;

    public List<Product> execute() {
        return productRepository.findAllProducts();
    }
}
