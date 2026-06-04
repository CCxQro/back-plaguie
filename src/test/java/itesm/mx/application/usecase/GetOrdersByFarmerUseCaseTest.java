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

    @Mock OrderRepository orderRepository;
    @Mock FarmerRepository farmerRepository;

    @InjectMocks
    GetOrdersByFarmerUseCase getOrdersByFarmerUseCase;

    private Farmer buildFarmer(Long farmerId) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(farmerId);
        return farmer;
    }

    private Order buildOrder(Long orderId, Long farmerId) {
        Farmer farmer = buildFarmer(farmerId);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(1L, "Pendiente");
        return new Order(orderId, farmer, seller, LocalDateTime.now(), status, BigDecimal.valueOf(500), List.of());
    }

    @Test
    void execute_ReturnsOrdersForFarmer() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer(1L)));
        when(orderRepository.findAllByFarmerId(1L)).thenReturn(
                List.of(buildOrder(1L, 1L), buildOrder(2L, 1L)));

        List<OrderResponseDto> result = getOrdersByFarmerUseCase.execute(6L);

        assertEquals(2, result.size());
        verify(orderRepository).findAllByFarmerId(1L);
    }

    @Test
    void execute_WhenNoOrders_ReturnsEmptyList() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer(1L)));
        when(orderRepository.findAllByFarmerId(1L)).thenReturn(List.of());

        List<OrderResponseDto> result = getOrdersByFarmerUseCase.execute(6L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenFarmerNotFound_ThrowsIllegalArgumentException() {
        when(farmerRepository.findByIdUser(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> getOrdersByFarmerUseCase.execute(99L));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getOrdersByFarmerUseCase.execute(null));
        verifyNoInteractions(farmerRepository, orderRepository);
    }
}
