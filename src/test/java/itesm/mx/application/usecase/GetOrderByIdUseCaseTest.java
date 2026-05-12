package itesm.mx.application.usecase;

import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.usecase.order.GetOrderByIdUseCase;
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
class GetOrderByIdUseCaseTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    GetOrderByIdUseCase getOrderByIdUseCase;

    private Order buildOrder(Long id) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(1L);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(1L, "Pendiente");
        return new Order(id, farmer, seller, LocalDateTime.now(), status, BigDecimal.valueOf(500), List.of());
    }

    @Test
    void execute_HappyPath_ReturnsMappedDto() {
        Order order = buildOrder(1L);
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        OrderResponseDto result = getOrderByIdUseCase.execute(1L);

        assertNotNull(result);
        assertEquals(1L, result.orderId);
        verify(orderRepository).findByIdWithDetails(1L);
    }

    @Test
    void execute_WhenNotFound_ThrowsIllegalStateException() {
        when(orderRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> getOrderByIdUseCase.execute(99L));
    }

    @Test
    void execute_WhenIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getOrderByIdUseCase.execute(null));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenIdIsZero_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getOrderByIdUseCase.execute(0L));
        verifyNoInteractions(orderRepository);
    }
}
