package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.infrastructure.persistence.entity.marketplace.PriceEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;

public class PriceMapper {

    public static PriceEntity toEntity(Price price) {
        PriceEntity entity = new PriceEntity();
        entity.priceId = price.getPriceId();
        entity.skuSellerId = price.getProduct().getSkuSellerId();
        entity.price = price.getPrice();
        entity.priceDate = price.getPriceDate();
        return entity;
    }

    public static Price toDomain(PriceEntity entity) {
        Price price = new Price();
        price.setPriceId(entity.priceId);
        price.setProduct(mapProduct(entity));
        price.setPrice(entity.price);
        price.setPriceDate(entity.priceDate);
        return price;
    }

    private static Product mapProduct(PriceEntity entity) {
        ProductEntity productEntity = entity.product;
        if (productEntity != null) {
            return ProductMapper.toDomain(productEntity);
        }
        Product product = new Product();
        product.setSkuSellerId(entity.skuSellerId);
        return product;
    }
}