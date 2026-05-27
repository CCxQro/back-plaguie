package itesm.mx.application.usecase;

import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.RealizarCompraDto;
import itesm.mx.application.dto.RealizarCompraItemDto;
import itesm.mx.application.usecase.marketplace.inventory.RegisterInventoryUseCase;
import itesm.mx.application.usecase.order.RealizarCompraUseCase;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryActionConstants;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.order.OrderDetailRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.order.OrderStatusRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class RealizarCompraUseCaseTest {

    @Mock FarmerRepository farmerRepository;
    @Mock ProductRepository productRepository;
    @Mock PriceRepository priceRepository;
    @Mock InventoryRepository inventoryRepository;
    @Mock OrderRepository orderRepository;
    @Mock OrderDetailRepository orderDetailRepository;
    @Mock OrderStatusRepository orderStatusRepository;
    @Mock RegisterInventoryUseCase registerInventoryUseCase;

    @InjectMocks RealizarCompraUseCase realizarCompraUseCase;

    private RealizarCompraDto dto(RealizarCompraItemDto... items) {
        RealizarCompraDto dto = new RealizarCompraDto();
        dto.items = List.of(items);
        return dto;
    }

    private RealizarCompraItemDto item(Long productId, Integer quantity) {
        RealizarCompraItemDto item = new RealizarCompraItemDto();
        item.productId = productId;
        item.quantity = quantity;
        return item;
    }

    private Farmer farmer() {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(7L);
        return farmer;
    }

    private TechnicalSeller seller(Long sellerId) {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(sellerId);
        return seller;
    }

    private Product product(Long productId, Long sellerId) {
        Product product = new Product();
        product.setSkuSellerId(productId);
        product.setName("Producto " + productId);
        product.setSeller(seller(sellerId));
        return product;
    }

    private Price price(Product product, String amount) {
        return new Price(1L, product, new BigDecimal(amount), LocalDateTime.now());
    }

    private Order savedOrder(Long orderId, Long sellerId) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setFarmer(farmer());
        order.setSeller(seller(sellerId));
        order.setOrderStatus(new OrderStatus(1L, "Pendiente"));
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    @Test
    void execute_SingleSellerCart_CreatesOrderAndSubtractsInventory() {
        Product product = product(1001L, 3L);
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(new OrderStatus(1L, "Pendiente")));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(product));
        when(priceRepository.findLatestBySkuSellerId(1001L)).thenReturn(Optional.of(price(product, "125.50")));
        when(inventoryRepository.currentStock(1001L)).thenReturn(10);
        when(orderRepository.save(any())).thenReturn(savedOrder(77L, 3L));
        when(orderDetailRepository.saveAll(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<OrderDetail> details = invocation.getArgument(0, List.class);
            return details;
        });

        List<OrderResponseDto> result = realizarCompraUseCase.execute(20L, dto(item(1001L, 2)));

        assertEquals(1, result.size());
        assertEquals(77L, result.get(0).orderId);
        assertEquals(BigDecimal.valueOf(251.0).setScale(2), result.get(0).totalAmount.setScale(2));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(registerInventoryUseCase).execute(inventoryCaptor.capture());
        assertEquals(1001L, inventoryCaptor.getValue().getProduct().getSkuSellerId());
        assertEquals(2, inventoryCaptor.getValue().getCantidad());
        assertEquals(InventoryActionConstants.SUBTRACT,
                inventoryCaptor.getValue().getAction().getInventoryActionId());
    }

    @Test
    void execute_MixedSellerCart_CreatesOneOrderPerSeller() {
        Product first = product(1001L, 3L);
        Product second = product(1002L, 4L);
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(new OrderStatus(1L, "Pendiente")));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(first));
        when(productRepository.findByProductId(1002L)).thenReturn(Optional.of(second));
        when(priceRepository.findLatestBySkuSellerId(1001L)).thenReturn(Optional.of(price(first, "100.00")));
        when(priceRepository.findLatestBySkuSellerId(1002L)).thenReturn(Optional.of(price(second, "50.00")));
        when(inventoryRepository.currentStock(1001L)).thenReturn(10);
        when(inventoryRepository.currentStock(1002L)).thenReturn(10);
        when(orderRepository.save(any()))
                .thenReturn(savedOrder(1L, 3L))
                .thenReturn(savedOrder(2L, 4L));
        when(orderDetailRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<OrderResponseDto> result = realizarCompraUseCase.execute(
                20L,
                dto(item(1001L, 1), item(1002L, 2)));

        assertEquals(2, result.size());
        verify(orderRepository, times(2)).save(any());
        verify(registerInventoryUseCase, times(2)).execute(any());
    }

    @Test
    void execute_WhenStockInsufficient_DoesNotPersistAnything() {
        Product product = product(1001L, 3L);
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(new OrderStatus(1L, "Pendiente")));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(product));
        when(priceRepository.findLatestBySkuSellerId(1001L)).thenReturn(Optional.of(price(product, "100.00")));
        when(inventoryRepository.currentStock(1001L)).thenReturn(1);

        assertThrows(IllegalArgumentException.class,
                () -> realizarCompraUseCase.execute(20L, dto(item(1001L, 2))));

        verifyNoInteractions(orderRepository, orderDetailRepository);
        verifyNoInteractions(registerInventoryUseCase);
    }

    @Test
    void execute_WhenProductMissing_ThrowsIllegalArgumentException() {
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(new OrderStatus(1L, "Pendiente")));
        when(productRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> realizarCompraUseCase.execute(20L, dto(item(999L, 1))));
        verifyNoInteractions(orderRepository, orderDetailRepository);
    }

    @Test
    void execute_WhenFarmerMissing_ThrowsIllegalArgumentException() {
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> realizarCompraUseCase.execute(20L, dto(item(1001L, 1))));
        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void execute_WhenProductHasNoSeller_ThrowsIllegalArgumentException() {
        Product product = product(1001L, 3L);
        product.setSeller(null);
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(new OrderStatus(1L, "Pendiente")));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class,
                () -> realizarCompraUseCase.execute(20L, dto(item(1001L, 1))));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenProductHasInvalidPrice_ThrowsIllegalArgumentException() {
        Product product = product(1001L, 3L);
        when(farmerRepository.findByIdUser(20L)).thenReturn(Optional.of(farmer()));
        when(orderStatusRepository.findStatusById(1L)).thenReturn(Optional.of(new OrderStatus(1L, "Pendiente")));
        when(productRepository.findByProductId(1001L)).thenReturn(Optional.of(product));
        when(priceRepository.findLatestBySkuSellerId(1001L)).thenReturn(Optional.of(price(product, "0.00")));

        assertThrows(IllegalArgumentException.class,
                () -> realizarCompraUseCase.execute(20L, dto(item(1001L, 1))));
        verifyNoInteractions(orderRepository);
    }
}
