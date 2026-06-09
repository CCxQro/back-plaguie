package itesm.mx.application.usecase.order;

import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareOrderUseCaseTest {

    @Mock OrderRepository orderRepository;

    @InjectMocks
    ShareOrderUseCase shareOrderUseCase;

    private Order buildOrder(Long orderId, Boolean providerShared) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(1L);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(1L, "Pendiente");
        Order order = new Order(orderId, farmer, seller, LocalDateTime.now(), status, BigDecimal.valueOf(500), List.of());
        order.setProviderShared(providerShared);
        return order;
    }

    @Test
    void execute_SharesOrderSuccessfully() {
        when(orderRepository.findOrderById(1L)).thenReturn(Optional.of(buildOrder(1L, false)));
        when(orderRepository.updateProviderShared(1L, true)).thenReturn(buildOrder(1L, true));

        OrderResponseDto result = shareOrderUseCase.execute(1L);

        assertNotNull(result);
        assertEquals(true, result.providerShared);
        verify(orderRepository).updateProviderShared(1L, true);
    }

    @Test
    void execute_WhenOrderNotFound_ThrowsIllegalStateException() {
        when(orderRepository.findOrderById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> shareOrderUseCase.execute(99L));
        verify(orderRepository, never()).updateProviderShared(any(), any());
    }
}
