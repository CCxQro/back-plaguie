package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findByProductId(Long skuSellerId);
    Product update(Long skuSellerId, Product product);
    boolean delete(Long skuSellerId);
    List<Product> findAllProducts();
    List<Product> findAllBySellerId(Long sellerId);
    List<Product> findAllByProviderId(Long providerId);
    List<Product> findAllByStatusId(Long statusId);
    long countAllProducts();
    long countProductsByStockAbove(int threshold);
    long countProductsByStockBetween(int minInclusive, int maxInclusive);
    long countProductsByStockBelow(int threshold);
}