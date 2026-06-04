package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.repository.order.OrderRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ShareOrderUseCase {

    private static final Logger log = Logger.getLogger(ShareOrderUseCase.class);

    @Inject
    OrderRepository orderRepository;

    @Transactional
    public OrderResponseDto execute(Long orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new IllegalStateException("Pedido no encontrado con id: " + orderId));
        
        // Notify the provider
        log.info("Notifying provider for order: " + orderId);
        
        // Update the database to reflect it was shared
        Order updatedOrder = orderRepository.updateProviderShared(orderId, true);
        
        return OrderDtoMapper.toResponseDto(updatedOrder);
    }
}
