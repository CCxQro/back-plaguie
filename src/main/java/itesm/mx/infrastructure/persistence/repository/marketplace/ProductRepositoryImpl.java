package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.infrastructure.mapper.marketplace.ProductMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductRepositoryImpl implements PanacheRepositoryBase<ProductEntity, Long>, ProductRepository {

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductMapper.toEntity(product);
        persistAndFlush(entity);
        return ProductMapper.toDomain(entity);
    }

    @Override
    public Optional<Product> findByProductId(Long skuSellerId) {
        return findByIdOptional(skuSellerId).map(ProductMapper::toDomain);
    }

    @Override
    public Product update(Long skuSellerId, Product product) {
        ProductEntity entity = findByIdOptional(skuSellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + skuSellerId));
        entity.name = product.getName();
        entity.sku = product.getSku();
        entity.categoryId = product.getCategory().getCategoryId();
        entity.providerId = product.getProvider().getProviderId();
        entity.unitValue = product.getUnitValue();
        entity.unitId = product.getUnit().getUnitId();
        entity.description = product.getDescription();
        entity.statusId = product.getStatus().getStatusId();
        entity.firebaseImageId = product.getFirebaseImageId();
        flush();
        return ProductMapper.toDomain(entity);
    }

    @Override
    public boolean delete(Long skuSellerId) {
        Optional<ProductEntity> entity = findByIdOptional(skuSellerId);
        if (entity.isEmpty()) {
            return false;
        }
        delete(entity.get());
        return true;
    }

    @Override
    public List<Product> findAllProducts() {
        return listAll().stream()
                .map(ProductMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAllBySellerId(Long sellerId) {
        return find("sellerId", sellerId).stream()
                .map(ProductMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAllByProviderId(Long providerId) {
        return find("providerId", providerId).stream()
                .map(ProductMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAllByStatusId(Long statusId) {
        return find("statusId", statusId).stream()
                .map(ProductMapper::toDomain)
                .toList();
    }
}