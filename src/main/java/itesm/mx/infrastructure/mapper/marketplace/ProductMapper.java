package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.*;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.infrastructure.mapper.user.TechnicalSellerMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.*;
import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;

public class ProductMapper {

    public static ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.skuSellerId = product.getSkuSellerId();
        entity.sellerId = product.getSeller().getTechnicalSellerId();
        entity.name = product.getName();
        entity.sku = product.getSku();
        entity.categoryId = product.getCategory().getCategoryId();
        entity.providerId = product.getProvider().getProviderId();
        entity.unitValue = product.getUnitValue();
        entity.unitId = product.getUnit().getUnitId();
        entity.description = product.getDescription();
        entity.statusId = product.getStatus().getStatusId();
        return entity;
    }

    public static Product toDomain(ProductEntity entity) {
        Product product = new Product();
        product.setSkuSellerId(entity.skuSellerId);
        product.setSeller(mapSeller(entity));
        product.setName(entity.name);
        product.setSku(entity.sku);
        product.setCategory(mapCategory(entity));
        product.setProvider(mapProvider(entity));
        product.setUnitValue(entity.unitValue);
        product.setUnit(mapUnit(entity));
        product.setDescription(entity.description);
        product.setStatus(mapStatus(entity));
        return product;
    }

    private static TechnicalSeller mapSeller(ProductEntity entity) {
        TechnicalSellerEntity sellerEntity = entity.seller;
        if (sellerEntity != null) {
            return TechnicalSellerMapper.toDomain(sellerEntity);
        }
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(entity.sellerId);
        return seller;
    }

    private static Category mapCategory(ProductEntity entity) {
        CategoryEntity categoryEntity = entity.category;
        if (categoryEntity != null) {
            return CategoryMapper.toDomain(categoryEntity);
        }
        Category category = new Category();
        category.setCategoryId(entity.categoryId);
        return category;
    }

    private static Provider mapProvider(ProductEntity entity) {
        ProviderEntity providerEntity = entity.provider;
        if (providerEntity != null) {
            return ProviderMapper.toDomain(providerEntity);
        }
        Provider provider = new Provider();
        provider.setProviderId(entity.providerId);
        return provider;
    }

    private static Unit mapUnit(ProductEntity entity) {
        UnitEntity unitEntity = entity.unit;
        if (unitEntity != null) {
            return UnitMapper.toDomain(unitEntity);
        }
        Unit unit = new Unit();
        unit.setUnitId(entity.unitId);
        return unit;
    }

    private static Status mapStatus(ProductEntity entity) {
        StatusEntity statusEntity = entity.status;
        if (statusEntity != null) {
            return StatusMapper.toDomain(statusEntity);
        }
        Status status = new Status();
        status.setStatusId(entity.statusId);
        return status;
    }
}