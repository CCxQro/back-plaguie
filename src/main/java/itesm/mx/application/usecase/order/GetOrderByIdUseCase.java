package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.repository.order.OrderRepository;

@ApplicationScoped
public class GetOrderByIdUseCase {

    @Inject
    OrderRepository orderRepository;

    public OrderResponseDto execute(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("El id del pedido es requerido");
        }
        if (orderId <= 0) {
            throw new IllegalArgumentException("El id del pedido debe ser positivo");
        }
        return orderRepository.findByIdWithDetails(orderId)
                .map(OrderDtoMapper::toResponseDto)
                .orElseThrow(() -> new IllegalStateException(
                        "Pedido no encontrado con id: " + orderId));
    }
}
