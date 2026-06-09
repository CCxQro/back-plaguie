package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.user.FarmerRepository;

import java.util.List;

@ApplicationScoped
public class GetOrdersByFarmerUseCase {

    @Inject OrderRepository orderRepository;
    @Inject FarmerRepository farmerRepository;

    public List<OrderResponseDto> execute(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id de usuario es requerido");
        }
        Long farmerId = farmerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró agricultor para el usuario con id: " + userId))
                .getFarmerId();
        return orderRepository.findAllByFarmerId(farmerId)
                .stream()
                .map(OrderDtoMapper::toResponseDto)
                .toList();
    }
}
