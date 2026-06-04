package itesm.mx.application.usecase.admin;

import itesm.mx.application.dto.admin.AdminMetricsDto;
import itesm.mx.application.mapper.admin.AdminMetricsMapper;
import itesm.mx.domain.models.admin.AdminMetrics;
import itesm.mx.domain.repository.admin.AdminMetricsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAdminMetricsUseCaseTest {

    @Mock
    private AdminMetricsRepository repository;

    @Spy
    private AdminMetricsMapper mapper;

    @InjectMocks
    private GetAdminMetricsUseCase useCase;

    @Test
    void execute_ReturnsMappedDto() {
        AdminMetrics metrics = new AdminMetrics(100, 50, 200, 20, 300, 30);
        when(repository.getMetrics()).thenReturn(metrics);

        AdminMetricsDto result = useCase.execute();

        assertNotNull(result);
        assertEquals(100, result.totalUsers);
        assertEquals(50, result.totalProducts);
        assertEquals(200, result.totalSurveillanceRecords);
        assertEquals(20, result.recentSurveillanceRecords);
        assertEquals(300, result.totalOrders);
        assertEquals(30, result.recentOrders);
    }
}
