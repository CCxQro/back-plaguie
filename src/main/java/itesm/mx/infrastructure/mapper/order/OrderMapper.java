package itesm.mx.infrastructure.mapper.order;

import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.infrastructure.mapper.user.FarmerMapper;
import itesm.mx.infrastructure.mapper.user.TechnicalSellerMapper;
import itesm.mx.infrastructure.persistence.entity.order.OrderEntity;

import java.util.List;

public class OrderMapper {

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.farmerId = order.getFarmer().getFarmerId();
        entity.sellerId = order.getSeller().getTechnicalSellerId();
        entity.orderDate = order.getOrderDate();
        entity.orderStatusId = order.getOrderStatus().getOrderStatusId();
        entity.totalAmount = order.getTotalAmount();
        entity.providerShared = order.getProviderShared() != null ? order.getProviderShared() : false;
        entity.shippingStreet = order.getShippingStreet();
        entity.shippingCity = order.getShippingCity();
        entity.shippingState = order.getShippingState();
        entity.shippingLat = order.getShippingLat();
        entity.shippingLon = order.getShippingLon();
        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        Order order = new Order();
        order.setOrderId(entity.orderId);
        order.setFarmer(mapFarmer(entity));
        order.setSeller(mapSeller(entity));
        order.setOrderDate(entity.orderDate);
        order.setOrderStatus(mapOrderStatus(entity));
        order.setTotalAmount(entity.totalAmount);
        order.setDetails(mapDetails(entity));
        order.setProviderShared(entity.providerShared);
        order.setShippingStreet(entity.shippingStreet);
        order.setShippingCity(entity.shippingCity);
        order.setShippingState(entity.shippingState);
        order.setShippingLat(entity.shippingLat);
        order.setShippingLon(entity.shippingLon);
        return order;
    }

    private static Farmer mapFarmer(OrderEntity entity) {
        if (entity.farmer != null) {
            return FarmerMapper.toDomain(entity.farmer);
        }
        Farmer f = new Farmer();
        f.setFarmerId(entity.farmerId);
        return f;
    }

    private static TechnicalSeller mapSeller(OrderEntity entity) {
        if (entity.seller != null) {
            return TechnicalSellerMapper.toDomain(entity.seller);
        }
        TechnicalSeller s = new TechnicalSeller();
        s.setTechnicalSellerId(entity.sellerId);
        return s;
    }

    private static OrderStatus mapOrderStatus(OrderEntity entity) {
        if (entity.orderStatus != null) {
            return OrderStatusMapper.toDomain(entity.orderStatus);
        }
        OrderStatus s = new OrderStatus();
        s.setOrderStatusId(entity.orderStatusId);
        return s;
    }

    private static List<OrderDetail> mapDetails(OrderEntity entity) {
        if (entity.details == null) {
            return null;
        }
        return entity.details.stream().map(OrderDetailMapper::toDomain).toList();
    }
}
