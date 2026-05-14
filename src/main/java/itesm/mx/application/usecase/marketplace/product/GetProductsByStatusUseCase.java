package itesm.mx.application.usecase.marketplace.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.marketplace.StatusRepository;

import java.util.List;

@ApplicationScoped
public class GetProductsByStatusUseCase {

    @Inject ProductRepository productRepository;
    @Inject StatusRepository statusRepository;
    @Inject PriceRepository priceRepository;
    @Inject InventoryRepository inventoryRepository;

    public List<Product> execute(Long statusId) {
        if (statusId == null) {
            throw new IllegalArgumentException("statusId is required");
        }
        statusRepository.findByStatusId(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));
        List<Product> products = productRepository.findAllByStatusId(statusId);
        products.forEach(p -> {
            priceRepository.findLatestBySkuSellerId(p.getSkuSellerId())
                    .ifPresent(latest -> {
                        p.setLatestPrice(latest.getPrice());
                        p.setLatestPriceDate(latest.getPriceDate());
                    });
            p.setStock(inventoryRepository.currentStock(p.getSkuSellerId()));
        });
        return products;
    }
}
