package itesm.mx.application.usecase.admin;

import itesm.mx.application.dto.admin.AdminMetricsDto;
import itesm.mx.application.mapper.admin.AdminMetricsMapper;
import itesm.mx.domain.models.admin.AdminMetrics;
import itesm.mx.domain.repository.admin.AdminMetricsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetAdminMetricsUseCase {
    
    private final AdminMetricsRepository repository;
    private final AdminMetricsMapper mapper;

    @Inject
    public GetAdminMetricsUseCase(AdminMetricsRepository repository, AdminMetricsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AdminMetricsDto execute() {
        AdminMetrics metrics = repository.getMetrics();
        return mapper.toDto(metrics);
    }
}
