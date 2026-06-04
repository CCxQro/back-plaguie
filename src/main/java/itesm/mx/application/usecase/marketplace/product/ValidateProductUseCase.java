package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.marketplace.StatusRepository;
import java.util.Set;

@ApplicationScoped
public class ValidateProductUseCase {

    private static final Set<Long> VALID_VALIDATION_STATUSES = Set.of(1L, 2L, 3L);

    @Inject
    ProductRepository productRepository;
    
    @Inject
    StatusRepository statusRepository;

    @Transactional
    public Product execute(Long skuSellerId, Long statusId) {
        if (skuSellerId == null || skuSellerId <= 0) {
            throw new IllegalArgumentException("El ID del producto no es válido");
        }
        if (statusId == null || !VALID_VALIDATION_STATUSES.contains(statusId)) {
            throw new IllegalArgumentException("El statusId debe ser 1 (Accepted), 2 (Revision) o 3 (Rejected)");
        }

        Product existingProduct = productRepository.findByProductId(skuSellerId)
                .orElseThrow(() -> new IllegalStateException("Producto no encontrado"));

        statusRepository.findByStatusId(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status no encontrado"));

        Status newStatus = new Status();
        newStatus.setStatusId(statusId);
        existingProduct.setStatus(newStatus);

        return productRepository.update(skuSellerId, existingProduct);
    }
}
