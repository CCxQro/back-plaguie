package itesm.mx.application.usecase;

import itesm.mx.application.dto.OrderStatusResponseDto;
import itesm.mx.application.usecase.order.GetAllOrderStatusesUseCase;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.repository.order.OrderStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllOrderStatusesUseCaseTest {

    @Mock
    OrderStatusRepository orderStatusRepository;

    @InjectMocks
    GetAllOrderStatusesUseCase getAllOrderStatusesUseCase;

    @Test
    void execute_ReturnsAllStatusesMapped() {
        when(orderStatusRepository.findAllStatuses()).thenReturn(List.of(
                new OrderStatus(1L, "Pendiente"),
                new OrderStatus(2L, "Confirmado")
        ));

        List<OrderStatusResponseDto> result = getAllOrderStatusesUseCase.execute();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).orderStatusId);
        assertEquals("Pendiente", result.get(0).estado);
        assertEquals(2L, result.get(1).orderStatusId);
        assertEquals("Confirmado", result.get(1).estado);
    }

    @Test
    void execute_WhenEmpty_ReturnsEmptyList() {
        when(orderStatusRepository.findAllStatuses()).thenReturn(List.of());

        List<OrderStatusResponseDto> result = getAllOrderStatusesUseCase.execute();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
