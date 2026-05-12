package itesm.mx.infrastructure.mapper.order;

import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.infrastructure.mapper.marketplace.ProductMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;
import itesm.mx.infrastructure.persistence.entity.order.OrderDetailEntity;

public class OrderDetailMapper {

    public static OrderDetailEntity toEntity(OrderDetail detail) {
        OrderDetailEntity entity = new OrderDetailEntity();
        entity.orderId = detail.getOrderId();
        entity.productId = detail.getProduct().getSkuSellerId();
        entity.quantity = detail.getQuantity();
        entity.unitPrice = detail.getUnitPrice();
        return entity;
    }

    public static OrderDetail toDomain(OrderDetailEntity entity) {
        OrderDetail detail = new OrderDetail();
        detail.setDetailId(entity.detailId);
        detail.setOrderId(entity.orderId);
        detail.setProduct(mapProduct(entity));
        detail.setQuantity(entity.quantity);
        detail.setUnitPrice(entity.unitPrice);
        return detail;
    }

    private static Product mapProduct(OrderDetailEntity entity) {
        ProductEntity productEntity = entity.product;
        if (productEntity != null) {
            return ProductMapper.toDomain(productEntity);
        }
        Product p = new Product();
        p.setSkuSellerId(entity.productId);
        return p;
    }
}
