package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.user.FarmerRepository;

import java.util.List;

@ApplicationScoped
public class GetOrdersByFarmerUseCase {

    @Inject FarmerRepository farmerRepository;
    @Inject OrderRepository orderRepository;

    public List<OrderResponseDto> execute(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario es requerido");
        }

        Farmer farmer = farmerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agricultor no encontrado para el usuario: " + userId));

        return orderRepository.findAllByFarmerIdWithDetails(farmer.getFarmerId())
                .stream()
                .map(OrderDtoMapper::toResponseDto)
                .toList();
    }
}
