package itesm.mx.application.mapper.order;

import itesm.mx.application.dto.FarmerLocationDto;
import itesm.mx.application.dto.OrderDetailResponseDto;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.OrderStatusResponseDto;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;

import java.util.List;

public class OrderDtoMapper {

    public static OrderResponseDto toResponseDto(Order order) {
        List<OrderDetailResponseDto> detailDtos = order.getDetails() == null ? null
                : order.getDetails().stream().map(OrderDtoMapper::toDetailResponseDto).toList();

        return new OrderResponseDto(
                order.getOrderId(),
                order.getFarmer().getFarmerId(),
                resolveFarmerName(order.getFarmer()),
                order.getSeller().getTechnicalSellerId(),
                resolveSellerName(order),
                order.getOrderDate(),
                order.getOrderStatus().getOrderStatusId(),
                order.getOrderStatus().getEstado(),
                order.getTotalAmount(),
                detailDtos
        );
    }

    public static OrderDetailResponseDto toDetailResponseDto(OrderDetail detail) {
        String productName = detail.getProduct() != null ? detail.getProduct().getName() : null;
        return new OrderDetailResponseDto(
                detail.getDetailId(),
                detail.getProduct() != null ? detail.getProduct().getSkuSellerId() : null,
                productName,
                detail.getQuantity(),
                detail.getUnitPrice()
        );
    }

    public static OrderStatusResponseDto toStatusResponseDto(OrderStatus status) {
        return new OrderStatusResponseDto(status.getOrderStatusId(), status.getEstado());
    }

    public static FarmerLocationDto toFarmerLocationDto(Order order) {
        Farmer farmer = order.getFarmer();
        Location location = farmer != null ? farmer.getLocation() : null;

        Double latitude = null;
        Double longitude = null;
        Long locationId = null;

        if (location != null) {
            locationId = location.getLocationId();
            if (location.getCoordinates() != null) {
                latitude = location.getCoordinates().getY();
                longitude = location.getCoordinates().getX();
            }
        }

        return new FarmerLocationDto(
                farmer != null ? farmer.getFarmerId() : null,
                resolveFarmerName(farmer),
                order.getOrderId(),
                latitude,
                longitude,
                locationId
        );
    }

    private static String resolveFarmerName(Farmer farmer) {
        if (farmer == null) return null;
        if (farmer.getUser() != null) return farmer.getUser().getName();
        return null;
    }

    private static String resolveSellerName(Order order) {
        if (order.getSeller() == null) return null;
        if (order.getSeller().getUser() != null) return order.getSeller().getUser().getName();
        return null;
    }
}
