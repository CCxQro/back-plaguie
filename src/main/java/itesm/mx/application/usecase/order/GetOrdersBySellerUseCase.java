package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;

import java.util.List;

@ApplicationScoped
public class GetOrdersBySellerUseCase {

    @Inject OrderRepository orderRepository;
    @Inject TechnicalSellerRepository technicalSellerRepository;

    public List<OrderResponseDto> execute(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id de usuario es requerido");
        }
        Long sellerId = technicalSellerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró vendedor para el usuario con id: " + userId))
                .getTechnicalSellerId();
        return orderRepository.findAllBySellerId(sellerId)
                .stream()
                .map(OrderDtoMapper::toResponseDto)
                .toList();
    }
}
