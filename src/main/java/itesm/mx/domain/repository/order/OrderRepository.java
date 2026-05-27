package itesm.mx.domain.repository.order;

import itesm.mx.domain.models.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findOrderById(Long orderId);
    Optional<Order> findByIdWithDetails(Long orderId);
    List<Order> findAllBySellerId(Long sellerId);
    List<Order> findAllBySellerIdWithFarmerLocation(Long sellerId);
    List<Order> findAllBySellerIdAndFarmerId(Long sellerId, Long farmerId);
    List<Order> findAllByFarmerIdWithDetails(Long farmerId);
    Order updateStatus(Long orderId, Long orderStatusId);
}
