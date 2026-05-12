package itesm.mx.domain.repository.order;

import itesm.mx.domain.models.order.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderStatusRepository {
    List<OrderStatus> findAllStatuses();
    Optional<OrderStatus> findStatusById(Long id);
    OrderStatus save(OrderStatus status);
}
