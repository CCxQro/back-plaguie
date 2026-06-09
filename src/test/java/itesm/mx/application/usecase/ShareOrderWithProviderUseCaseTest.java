package itesm.mx.application.usecase;

import itesm.mx.application.dto.ShareOrderResponseDto;
import itesm.mx.application.usecase.order.ShareOrderWithProviderUseCase;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.order.FarmerDataSharing;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.order.FarmerDataSharingRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareOrderWithProviderUseCaseTest {

    @Mock OrderRepository orderRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock FarmerDataSharingRepository farmerDataSharingRepository;
    @Mock ParcelaRepository parcelaRepository;

    @InjectMocks
    ShareOrderWithProviderUseCase shareOrderWithProviderUseCase;

    private Farmer buildFarmer(Long farmerId) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(farmerId);
        return farmer;
    }

    private Order buildOrderWithDetails(Long orderId, Long farmerId, Long providerId) {
        Farmer farmer = buildFarmer(farmerId);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(1L, "Pendiente");

        Provider provider = new Provider();
        provider.setProviderId(providerId);
        Product product = new Product();
        product.setSkuSellerId(1001L);
        product.setProvider(provider);

        OrderDetail detail = new OrderDetail(1L, orderId, product, 2, 100.0f);
        return new Order(orderId, farmer, seller, LocalDateTime.now(), status, BigDecimal.valueOf(200), List.of(detail));
    }

    @Test
    void execute_HappyPath_SharesOrderSuccessfully() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer(1L)));
        when(orderRepository.findByIdWithDetails(5L))
                .thenReturn(Optional.of(buildOrderWithDetails(5L, 1L, 2L)));
        when(parcelaRepository.findByFarmerId(1L)).thenReturn(List.of());
        FarmerDataSharing saved = new FarmerDataSharing(1L, 5L, 1L, 2L, LocalDateTime.now(), "{}");
        when(farmerDataSharingRepository.save(any())).thenReturn(saved);

        ShareOrderResponseDto result = shareOrderWithProviderUseCase.execute(5L, 6L);

        assertNotNull(result);
        assertEquals(5L, result.orderId);
        assertTrue(result.shared);
        assertNotNull(result.message);
        verify(farmerDataSharingRepository).save(any());
    }

    @Test
    void execute_WhenOrderIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> shareOrderWithProviderUseCase.execute(null, 6L));
        verifyNoInteractions(orderRepository, farmerRepository);
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> shareOrderWithProviderUseCase.execute(5L, null));
        verifyNoInteractions(orderRepository, farmerRepository);
    }

    @Test
    void execute_WhenFarmerNotFound_ThrowsIllegalStateException() {
        when(farmerRepository.findByIdUser(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,
                () -> shareOrderWithProviderUseCase.execute(5L, 99L));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenOrderNotFound_ThrowsIllegalStateException() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer(1L)));
        when(orderRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,
                () -> shareOrderWithProviderUseCase.execute(99L, 6L));
    }

    @Test
    void execute_WhenOrderDoesNotBelongToFarmer_ThrowsSecurityException() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer(1L)));
        // Order belongs to farmer 2, but authenticated user is farmer 1
        when(orderRepository.findByIdWithDetails(5L))
                .thenReturn(Optional.of(buildOrderWithDetails(5L, 2L, 2L)));
        assertThrows(SecurityException.class,
                () -> shareOrderWithProviderUseCase.execute(5L, 6L));
        verifyNoInteractions(farmerDataSharingRepository);
    }
}
