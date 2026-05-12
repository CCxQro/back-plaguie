package itesm.mx.application.usecase;

import itesm.mx.application.dto.OrderDetailItemDto;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.RegisterOrderDto;
import itesm.mx.application.usecase.order.CreateOrderUseCase;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.order.OrderDetailRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.order.OrderStatusRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderDetailRepository orderDetailRepository;
    @Mock OrderStatusRepository orderStatusRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock TechnicalSellerRepository technicalSellerRepository;
    @Mock ProductRepository productRepository;

    @InjectMocks
    CreateOrderUseCase createOrderUseCase;

    private RegisterOrderDto validDto() {
        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = 1L;
        dto.sellerId = 1L;
        dto.orderStatusId = 1L;
        dto.totalAmount = BigDecimal.valueOf(500);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = 1001L;
        item.quantity = 2;
        item.unitPrice = 250.0f;
        dto.details = List.of(item);
        return dto;
    }

    private Order savedOrder() {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(1L);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(1L, "Pendiente");
        return new Order(1L, farmer, seller, LocalDateTime.now(), status, BigDecimal.valueOf(500), null);
    }

    private OrderDetail savedDetail() {
        Product p = new Product();
        p.setSkuSellerId(1001L);
        p.setName("Fertilizante");
        return new OrderDetail(1L, 1L, p, 2, 250.0f);
    }

    @Test
    void execute_HappyPath_PersistsOrderAndDetails() {
        Farmer farmer = new Farmer(); farmer.setFarmerId(1L);
        TechnicalSeller seller = new TechnicalSeller(); seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(1L, "Pendiente");
        Product product = new Product(); product.setSkuSellerId(1001L); product.setName("Fertilizante");

        when(farmerRepository.findByFarmerId(1L)).thenReturn(Optional.of(farmer));
        when(technicalSellerRepository.findByTechnicalSellerId(1L)).thenReturn(Optional.of(seller));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(status));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenReturn(savedOrder());
        when(orderDetailRepository.saveAll(any())).thenReturn(List.of(savedDetail()));

        OrderResponseDto result = createOrderUseCase.execute(validDto());

        assertNotNull(result);
        assertEquals(1L, result.orderId);
        assertNotNull(result.details);
        assertEquals(1, result.details.size());
    }

    @Test
    void execute_WhenDtoIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> createOrderUseCase.execute(null));
        verifyNoInteractions(orderRepository, farmerRepository, technicalSellerRepository);
    }

    @Test
    void execute_WhenFarmerIdIsNull_ThrowsIllegalArgumentException() {
        RegisterOrderDto dto = validDto();
        dto.farmerId = null;
        assertThrows(IllegalArgumentException.class, () -> createOrderUseCase.execute(dto));
        verifyNoInteractions(farmerRepository);
    }

    @Test
    void execute_WhenDetailsEmpty_ThrowsIllegalArgumentException() {
        RegisterOrderDto dto = validDto();
        dto.details = List.of();
        assertThrows(IllegalArgumentException.class, () -> createOrderUseCase.execute(dto));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenFarmerNotFound_ThrowsIllegalArgumentException() {
        when(farmerRepository.findByFarmerId(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> createOrderUseCase.execute(validDto()));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenProductNotFound_ThrowsIllegalArgumentException() {
        Farmer farmer = new Farmer(); farmer.setFarmerId(1L);
        TechnicalSeller seller = new TechnicalSeller(); seller.setTechnicalSellerId(1L);
        OrderStatus status = new OrderStatus(1L, "Pendiente");

        when(farmerRepository.findByFarmerId(1L)).thenReturn(Optional.of(farmer));
        when(technicalSellerRepository.findByTechnicalSellerId(1L)).thenReturn(Optional.of(seller));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(status));
        Order saved = savedOrder();
        when(orderRepository.save(any())).thenReturn(saved);
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> createOrderUseCase.execute(validDto()));
    }
}
