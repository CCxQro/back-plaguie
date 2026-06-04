package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.BuyOrderDto;
import itesm.mx.application.dto.BuyOrderItemDto;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BuyOrderUseCase {

    private static final String PENDING_STATUS = "Pendiente";

    @Inject OrderRepository orderRepository;
    @Inject OrderDetailRepository orderDetailRepository;
    @Inject OrderStatusRepository orderStatusRepository;
    @Inject FarmerRepository farmerRepository;
    @Inject ProductRepository productRepository;
    @Inject InventoryRepository inventoryRepository;

    @Transactional
    public OrderResponseDto execute(Long userId, BuyOrderDto dto) {
        if (userId == null) {
            throw new IllegalArgumentException("El id de usuario es requerido");
        }
        if (dto == null) {
            throw new IllegalArgumentException("El dto del pedido es requerido");
        }
        if (dto.items == null || dto.items.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un item");
        }
        if (dto.shippingAddress == null) {
            throw new IllegalArgumentException("La dirección de envío es requerida");
        }

        Farmer farmer = farmerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró agricultor para el usuario con id: " + userId));

        OrderStatus pendingStatus = orderStatusRepository.findAllStatuses().stream()
                .filter(s -> PENDING_STATUS.equalsIgnoreCase(s.getEstado()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el estado 'Pendiente' en el catálogo"));

        // Resolve products and validate stock
        List<Product> resolvedProducts = new ArrayList<>();
        for (BuyOrderItemDto item : dto.items) {
            Product product = productRepository.findByProductId(item.productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado con id: " + item.productId));
            int stock = inventoryRepository.currentStock(product.getSkuSellerId());
            if (stock < item.quantity) {
                throw new IllegalStateException(
                        "Stock insuficiente para el producto con id: " + item.productId
                                + ". Disponible: " + stock + ", solicitado: " + item.quantity);
            }
            resolvedProducts.add(product);
        }

        // Determine seller from first product
        Product firstProduct = resolvedProducts.get(0);
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(firstProduct.getSeller().getTechnicalSellerId());

        // Calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < dto.items.size(); i++) {
            int qty = dto.items.get(i).quantity;
            BigDecimal unitPrice = resolvedProducts.get(i).getLatestPrice() != null
                    ? resolvedProducts.get(i).getLatestPrice()
                    : BigDecimal.ZERO;
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
        }

        Order order = new Order();
        order.setFarmer(farmer);
        order.setSeller(seller);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(pendingStatus);
        order.setTotalAmount(totalAmount);
        order.setShippingStreet(dto.shippingAddress.street);
        order.setShippingCity(dto.shippingAddress.city);
        order.setShippingState(dto.shippingAddress.state);
        order.setShippingLat(dto.shippingAddress.latitude);
        order.setShippingLon(dto.shippingAddress.longitude);

        Order savedOrder = orderRepository.save(order);

        List<OrderDetail> details = new ArrayList<>();
        for (int i = 0; i < dto.items.size(); i++) {
            BuyOrderItemDto item = dto.items.get(i);
            Product product = resolvedProducts.get(i);
            BigDecimal unitPrice = product.getLatestPrice() != null
                    ? product.getLatestPrice()
                    : BigDecimal.ZERO;
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(savedOrder.getOrderId());
            detail.setProduct(product);
            detail.setQuantity(item.quantity);
            detail.setUnitPrice(unitPrice.floatValue());
            details.add(detail);
        }

        List<OrderDetail> savedDetails = orderDetailRepository.saveAll(details);
        savedOrder.setDetails(savedDetails);

        return OrderDtoMapper.toResponseDto(savedOrder);
    }
}
