package itesm.mx.infrastructure.mapper.order;

import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.infrastructure.persistence.entity.order.OrderStatusEntity;

public class OrderStatusMapper {

    public static OrderStatusEntity toEntity(OrderStatus status) {
        OrderStatusEntity entity = new OrderStatusEntity();
        entity.orderStatusId = status.getOrderStatusId();
        entity.estado = status.getEstado();
        return entity;
    }

    public static OrderStatus toDomain(OrderStatusEntity entity) {
        return new OrderStatus(entity.orderStatusId, entity.estado);
    }
}
