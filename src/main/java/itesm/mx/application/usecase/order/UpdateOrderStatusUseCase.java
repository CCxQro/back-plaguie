package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.order.OrderStatusRepository;

@ApplicationScoped
public class UpdateOrderStatusUseCase {

    @Inject OrderRepository orderRepository;
    @Inject OrderStatusRepository orderStatusRepository;

    @Transactional
    public OrderResponseDto execute(Long orderId, Long orderStatusId) {
        if (orderId == null) {
            throw new IllegalArgumentException("El id del pedido es requerido");
        }
        if (orderStatusId == null) {
            throw new IllegalArgumentException("El id del estado es requerido");
        }
        orderStatusRepository.findStatusById(orderStatusId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estado de pedido no encontrado con id: " + orderStatusId));
        Order updated = orderRepository.updateStatus(orderId, orderStatusId);
        return OrderDtoMapper.toResponseDto(updated);
    }
}
