package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.OrderStatusResponseDto;
import itesm.mx.application.mapper.order.OrderDtoMapper;
import itesm.mx.domain.repository.order.OrderStatusRepository;

import java.util.List;

@ApplicationScoped
public class GetAllOrderStatusesUseCase {

    @Inject
    OrderStatusRepository orderStatusRepository;

    public List<OrderStatusResponseDto> execute() {
        return orderStatusRepository.findAllStatuses()
                .stream()
                .map(OrderDtoMapper::toStatusResponseDto)
                .toList();
    }
}
