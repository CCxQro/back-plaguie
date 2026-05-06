package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.marketplace.StatusRepository;

import java.util.List;

@ApplicationScoped
public class GetProductsByStatusUseCase {

    @Inject ProductRepository productRepository;
    @Inject StatusRepository statusRepository;

    public List<Product> execute(Long statusId) {
        if (statusId == null) {
            throw new IllegalArgumentException("statusId is required");
        }
        statusRepository.findByStatusId(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));
        return productRepository.findAllByStatusId(statusId);
    }
}
