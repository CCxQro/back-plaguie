package itesm.mx.application.usecase;

import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.usecase.order.UpdateOrderStatusUseCase;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.order.OrderStatusRepository;
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
class UpdateOrderStatusUseCaseTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderStatusRepository orderStatusRepository;

    @InjectMocks
    UpdateOrderStatusUseCase updateOrderStatusUseCase;

    private Order buildOrder(Long orderId, Long statusId, String statusName) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(1L);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(statusId, statusName);
        return new Order(orderId, farmer, seller, LocalDateTime.now(), status, BigDecimal.valueOf(500), List.of());
    }

    @Test
    void execute_UpdatesStatusSuccessfully() {
        when(orderStatusRepository.findStatusById(2L)).thenReturn(Optional.of(new OrderStatus(2L, "Confirmado")));
        when(orderRepository.updateStatus(1L, 2L)).thenReturn(buildOrder(1L, 2L, "Confirmado"));

        OrderResponseDto result = updateOrderStatusUseCase.execute(1L, 2L);

        assertNotNull(result);
        assertEquals(2L, result.orderStatusId);
        assertEquals("Confirmado", result.orderStatusName);
    }

    @Test
    void execute_WhenOrderNotFound_ThrowsIllegalStateException() {
        when(orderStatusRepository.findStatusById(2L)).thenReturn(Optional.of(new OrderStatus(2L, "Confirmado")));
        when(orderRepository.updateStatus(99L, 2L))
                .thenThrow(new IllegalStateException("Pedido no encontrado con id: 99"));

        assertThrows(IllegalStateException.class, () -> updateOrderStatusUseCase.execute(99L, 2L));
    }

    @Test
    void execute_WhenStatusNotFound_ThrowsIllegalArgumentException() {
        when(orderStatusRepository.findStatusById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> updateOrderStatusUseCase.execute(1L, 99L));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenOrderIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> updateOrderStatusUseCase.execute(null, 2L));
        verifyNoInteractions(orderStatusRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenStatusIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> updateOrderStatusUseCase.execute(1L, null));
        verifyNoInteractions(orderRepository);
    }
}
