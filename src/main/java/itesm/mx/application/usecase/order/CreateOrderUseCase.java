package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.RegisterOrderDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.order.OrderDetailRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.order.OrderStatusRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateOrderUseCase {

    @Inject OrderRepository orderRepository;
    @Inject OrderDetailRepository orderDetailRepository;
    @Inject OrderStatusRepository orderStatusRepository;
    @Inject FarmerRepository farmerRepository;
    @Inject TechnicalSellerRepository technicalSellerRepository;
    @Inject ProductRepository productRepository;

    @Transactional
    public OrderResponseDto execute(RegisterOrderDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El dto del pedido es requerido");
        }
        if (dto.farmerId == null) {
            throw new IllegalArgumentException("El id del agricultor es requerido");
        }
        if (dto.sellerId == null) {
            throw new IllegalArgumentException("El id del vendedor es requerido");
        }
        if (dto.details == null || dto.details.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un detalle");
        }

        Farmer farmer = farmerRepository.findByFarmerId(dto.farmerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agricultor no encontrado con id: " + dto.farmerId));

        TechnicalSeller seller = technicalSellerRepository.findByTechnicalSellerId(dto.sellerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vendedor no encontrado con id: " + dto.sellerId));

        OrderStatus status = orderStatusRepository.findStatusById(dto.orderStatusId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estado de pedido no encontrado con id: " + dto.orderStatusId));

        Order order = new Order();
        order.setFarmer(farmer);
        order.setSeller(seller);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(status);
        order.setTotalAmount(dto.totalAmount);

        Order saved = orderRepository.save(order);

        List<OrderDetail> details = dto.details.stream().map(item -> {
            Product product = productRepository.findByProductId(item.productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado con id: " + item.productId));
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(saved.getOrderId());
            detail.setProduct(product);
            detail.setQuantity(item.quantity);
            detail.setUnitPrice(item.unitPrice);
            return detail;
        }).toList();

        List<OrderDetail> savedDetails = orderDetailRepository.saveAll(details);
        saved.setDetails(savedDetails);

        return OrderDtoMapper.toResponseDto(saved);
    }
}
