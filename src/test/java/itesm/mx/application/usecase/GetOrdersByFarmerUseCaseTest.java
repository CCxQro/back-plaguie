package itesm.mx.application.usecase;

import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.usecase.order.GetOrdersByFarmerUseCase;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
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
class GetOrdersByFarmerUseCaseTest {

    @Mock FarmerRepository farmerRepository;
    @Mock OrderRepository orderRepository;

    @InjectMocks GetOrdersByFarmerUseCase getOrdersByFarmerUseCase;

    private Farmer farmer() {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(7L);
        return farmer;
    }

    private Order order(Long orderId) {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(3L);
        return new Order(orderId, farmer(), seller, LocalDateTime.now(),
                new OrderStatus(1L, "Pendiente"), BigDecimal.valueOf(100), List.of());
    }

    @Test
    void execute_ReturnsOrdersForCurrentFarmer() {
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderRepository.findAllByFarmerIdWithDetails(7L)).thenReturn(List.of(order(1L), order(2L)));

        List<OrderResponseDto> result = getOrdersByFarmerUseCase.execute(20L);

        assertEquals(2, result.size());
        verify(orderRepository).findAllByFarmerIdWithDetails(7L);
    }

    @Test
    void execute_WhenNoOrders_ReturnsEmptyList() {
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderRepository.findAllByFarmerIdWithDetails(7L)).thenReturn(List.of());

        List<OrderResponseDto> result = getOrdersByFarmerUseCase.execute(20L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenFarmerNotFound_ThrowsIllegalArgumentException() {
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> getOrdersByFarmerUseCase.execute(20L));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getOrdersByFarmerUseCase.execute(null));
        verifyNoInteractions(farmerRepository, orderRepository);
    }
}
