package itesm.mx.domain.repository.order;

import itesm.mx.domain.models.order.OrderDetail;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository {
    List<OrderDetail> saveAll(List<OrderDetail> details);
    List<OrderDetail> findAllByOrderId(Long orderId);
    Optional<OrderDetail> findDetailById(Long detailId);
}
