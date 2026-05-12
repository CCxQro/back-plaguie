package itesm.mx.application.usecase;

import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.usecase.order.GetOrdersBySellerUseCase;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
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
class GetOrdersBySellerUseCaseTest {

    @Mock OrderRepository orderRepository;
    @Mock TechnicalSellerRepository technicalSellerRepository;

    @InjectMocks
    GetOrdersBySellerUseCase getOrdersBySellerUseCase;

    private TechnicalSeller buildSeller(Long sellerId, Long userId) {
        TechnicalSeller s = new TechnicalSeller();
        s.setTechnicalSellerId(sellerId);
        return s;
    }

    private Order buildOrder(Long id, Long sellerId) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(1L);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(sellerId);
        OrderStatus status = new OrderStatus(1L, "Pendiente");
        return new Order(id, farmer, seller, LocalDateTime.now(), status, BigDecimal.valueOf(200), List.of());
    }

    @Test
    void execute_ReturnsOrdersForSeller() {
        when(technicalSellerRepository.findByIdUser(11L)).thenReturn(Optional.of(buildSeller(1L, 11L)));
        when(orderRepository.findAllBySellerId(1L)).thenReturn(List.of(buildOrder(1L, 1L), buildOrder(2L, 1L)));

        List<OrderResponseDto> result = getOrdersBySellerUseCase.execute(11L);

        assertEquals(2, result.size());
        verify(orderRepository).findAllBySellerId(1L);
    }

    @Test
    void execute_WhenNoOrders_ReturnsEmptyList() {
        when(technicalSellerRepository.findByIdUser(11L)).thenReturn(Optional.of(buildSeller(1L, 11L)));
        when(orderRepository.findAllBySellerId(1L)).thenReturn(List.of());

        List<OrderResponseDto> result = getOrdersBySellerUseCase.execute(11L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenSellerNotFound_ThrowsIllegalArgumentException() {
        when(technicalSellerRepository.findByIdUser(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> getOrdersBySellerUseCase.execute(99L));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getOrdersBySellerUseCase.execute(null));
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(technicalSellerRepository);
    }
}
