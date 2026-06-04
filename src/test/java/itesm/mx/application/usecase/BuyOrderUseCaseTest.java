package itesm.mx.application.usecase;

import itesm.mx.application.dto.BuyOrderDto;
import itesm.mx.application.dto.BuyOrderItemDto;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.usecase.order.BuyOrderUseCase;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.order.OrderDetailRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.order.OrderStatusRepository;
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
class BuyOrderUseCaseTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderDetailRepository orderDetailRepository;
    @Mock OrderStatusRepository orderStatusRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock ProductRepository productRepository;
    @Mock InventoryRepository inventoryRepository;

    @InjectMocks
    BuyOrderUseCase buyOrderUseCase;

    private BuyOrderDto validDto() {
        BuyOrderDto dto = new BuyOrderDto();
        BuyOrderItemDto item = new BuyOrderItemDto();
        item.productId = 1001L;
        item.quantity = 2;
        dto.items = List.of(item);
        BuyOrderDto.ShippingAddressDto address = new BuyOrderDto.ShippingAddressDto();
        address.street = "Av. Principal 123";
        address.city = "Guadalajara";
        address.state = "Jalisco";
        address.latitude = 20.75;
        address.longitude = -103.48;
        dto.shippingAddress = address;
        return dto;
    }

    private Farmer buildFarmer() {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(1L);
        return farmer;
    }

    private OrderStatus pendingStatus() {
        return new OrderStatus(1L, "Pendiente");
    }

    private Product buildProduct() {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        Product product = new Product();
        product.setSkuSellerId(1001L);
        product.setName("Fertilizante");
        product.setSeller(seller);
        product.setLatestPrice(BigDecimal.valueOf(250));
        return product;
    }

    private Order savedOrder() {
        Farmer farmer = buildFarmer();
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        return new Order(10L, farmer, seller, LocalDateTime.now(), pendingStatus(), BigDecimal.valueOf(500), null);
    }

    private OrderDetail savedDetail() {
        Product product = buildProduct();
        return new OrderDetail(1L, 10L, product, 2, 250.0f);
    }

    @Test
    void execute_HappyPath_CreatesOrderSuccessfully() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer()));
        when(orderStatusRepository.findAllStatuses()).thenReturn(List.of(pendingStatus()));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(buildProduct()));
        when(inventoryRepository.currentStock(1001L)).thenReturn(10);
        when(orderRepository.save(any())).thenReturn(savedOrder());
        when(orderDetailRepository.saveAll(any())).thenReturn(List.of(savedDetail()));

        OrderResponseDto result = buyOrderUseCase.execute(6L, validDto());

        assertNotNull(result);
        assertEquals(10L, result.orderId);
        verify(orderRepository).save(any());
        verify(orderDetailRepository).saveAll(any());
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> buyOrderUseCase.execute(null, validDto()));
        verifyNoInteractions(farmerRepository, orderRepository);
    }

    @Test
    void execute_WhenDtoIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> buyOrderUseCase.execute(6L, null));
        verifyNoInteractions(farmerRepository, orderRepository);
    }

    @Test
    void execute_WhenItemsEmpty_ThrowsIllegalArgumentException() {
        BuyOrderDto dto = validDto();
        dto.items = List.of();
        assertThrows(IllegalArgumentException.class, () -> buyOrderUseCase.execute(6L, dto));
        verifyNoInteractions(farmerRepository, orderRepository);
    }

    @Test
    void execute_WhenFarmerNotFound_ThrowsIllegalStateException() {
        when(farmerRepository.findByIdUser(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> buyOrderUseCase.execute(99L, validDto()));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenProductNotFound_ThrowsIllegalArgumentException() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer()));
        when(orderStatusRepository.findAllStatuses()).thenReturn(List.of(pendingStatus()));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> buyOrderUseCase.execute(6L, validDto()));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenStockInsufficient_ThrowsIllegalStateException() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer()));
        when(orderStatusRepository.findAllStatuses()).thenReturn(List.of(pendingStatus()));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(buildProduct()));
        when(inventoryRepository.currentStock(1001L)).thenReturn(1); // Only 1 in stock, need 2

        assertThrows(IllegalStateException.class, () -> buyOrderUseCase.execute(6L, validDto()));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenNoPendingStatus_ThrowsIllegalStateException() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(buildFarmer()));
        when(orderStatusRepository.findAllStatuses()).thenReturn(List.of(new OrderStatus(2L, "Confirmado")));

        assertThrows(IllegalStateException.class, () -> buyOrderUseCase.execute(6L, validDto()));
        verifyNoInteractions(orderRepository);
    }
}
