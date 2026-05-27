package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.RealizarCompraDto;
import itesm.mx.application.dto.RealizarCompraItemDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryAction;
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
import itesm.mx.application.usecase.marketplace.inventory.RegisterInventoryUseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RealizarCompraUseCase {

    private static final Long PENDING_STATUS_ID = 1L;

    @Inject FarmerRepository farmerRepository;
    @Inject ProductRepository productRepository;
    @Inject PriceRepository priceRepository;
    @Inject InventoryRepository inventoryRepository;
    @Inject OrderRepository orderRepository;
    @Inject OrderDetailRepository orderDetailRepository;
    @Inject OrderStatusRepository orderStatusRepository;
    @Inject RegisterInventoryUseCase registerInventoryUseCase;

    @Transactional
    public List<OrderResponseDto> execute(Long userId, RealizarCompraDto dto) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario es requerido");
        }
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la compra es requerido");
        }
        if (dto.items == null || dto.items.isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un producto");
        }

        Farmer farmer = farmerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agricultor no encontrado para el usuario: " + userId));
        OrderStatus pendingStatus = orderStatusRepository.findStatusById(PENDING_STATUS_ID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estado de pedido Pendiente no encontrado"));

        List<PurchaseLine> lines = validateAndBuildLines(dto.items);
        Map<Long, List<PurchaseLine>> linesBySeller = groupBySeller(lines);

        List<OrderResponseDto> responses = new ArrayList<>();
        for (Map.Entry<Long, List<PurchaseLine>> entry : linesBySeller.entrySet()) {
            responses.add(createOrderForSeller(farmer, pendingStatus, entry.getValue()));
        }
        return responses;
    }

    private List<PurchaseLine> validateAndBuildLines(List<RealizarCompraItemDto> items) {
        Map<Long, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (RealizarCompraItemDto item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Cada producto de la compra es requerido");
            }
            if (item.productId == null) {
                throw new IllegalArgumentException("El id del producto es requerido");
            }
            if (item.quantity == null || item.quantity <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            }
            quantitiesByProduct.merge(item.productId, item.quantity, Integer::sum);
        }

        List<PurchaseLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantitiesByProduct.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            Product product = productRepository.findByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado con id: " + productId));

            TechnicalSeller seller = product.getSeller();
            if (seller == null || seller.getTechnicalSellerId() == null) {
                throw new IllegalArgumentException(
                        "El producto no tiene vendedor asignado: " + productId);
            }

            Price latestPrice = priceRepository.findLatestBySkuSellerId(productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El producto no tiene precio disponible: " + productId));
            if (latestPrice.getPrice() == null || latestPrice.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "El producto no tiene precio valido: " + productId);
            }

            int currentStock = inventoryRepository.currentStock(productId);
            if (currentStock < quantity) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el producto " + productId
                                + " (disponible=" + currentStock + ", solicitado=" + quantity + ")");
            }

            lines.add(new PurchaseLine(product, quantity, latestPrice.getPrice()));
        }
        return lines;
    }

    private Map<Long, List<PurchaseLine>> groupBySeller(List<PurchaseLine> lines) {
        Map<Long, List<PurchaseLine>> grouped = new LinkedHashMap<>();
        for (PurchaseLine line : lines) {
            Long sellerId = line.product().getSeller().getTechnicalSellerId();
            grouped.computeIfAbsent(sellerId, ignored -> new ArrayList<>()).add(line);
        }
        return grouped;
    }

    private OrderResponseDto createOrderForSeller(
            Farmer farmer,
            OrderStatus pendingStatus,
            List<PurchaseLine> lines) {
        TechnicalSeller seller = lines.get(0).product().getSeller();
        BigDecimal total = lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setFarmer(farmer);
        order.setSeller(seller);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(pendingStatus);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        saved.setFarmer(farmer);
        saved.setSeller(seller);
        saved.setOrderStatus(pendingStatus);
        saved.setOrderDate(order.getOrderDate());
        saved.setTotalAmount(total);

        List<OrderDetail> details = lines.stream()
                .map(line -> {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderId(saved.getOrderId());
                    detail.setProduct(line.product());
                    detail.setQuantity(line.quantity());
                    detail.setUnitPrice(line.unitPrice().floatValue());
                    return detail;
                })
                .toList();

        List<OrderDetail> savedDetails = orderDetailRepository.saveAll(details);
        saved.setDetails(savedDetails);

        for (PurchaseLine line : lines) {
            Inventory movement = new Inventory();
            Product productRef = new Product();
            productRef.setSkuSellerId(line.product().getSkuSellerId());
            movement.setProduct(productRef);
            movement.setCantidad(line.quantity());
            InventoryAction action = new InventoryAction();
            action.setInventoryActionId(InventoryActionConstants.SUBTRACT);
            movement.setAction(action);
            registerInventoryUseCase.execute(movement);
        }

        return OrderDtoMapper.toResponseDto(saved);
    }

    private record PurchaseLine(Product product, Integer quantity, BigDecimal unitPrice) {}
}
