package itesm.mx.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ClientMapDto {
    public Long farmerId;
    public Long userId;
    public String name;
    public String email;
    public Double latitude;
    public Double longitude;
    public Long locationId;
    public String state;
    public String municipality;
    public String locality;
    public List<String> cultivos;
    public List<String> estadosParcela;
    public Integer parcelasCount;
    public Double totalHectareas;
    public Boolean hasActiveAlerts;
    public Integer activeAlertsCount;
    public String maxAlertSeverity;
    public Integer totalOrders;
    public LocalDateTime lastOrderDate;

    public ClientMapDto() {}

    public ClientMapDto(Long farmerId, Long userId, String name, String email,
                        Double latitude, Double longitude, Long locationId,
                        String state, String municipality, String locality,
                        List<String> cultivos, List<String> estadosParcela,
                        Integer parcelasCount, Double totalHectareas,
                        Boolean hasActiveAlerts, Integer activeAlertsCount,
                        String maxAlertSeverity,
                        Integer totalOrders, LocalDateTime lastOrderDate) {
        this.farmerId = farmerId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationId = locationId;
        this.state = state;
        this.municipality = municipality;
        this.locality = locality;
        this.cultivos = cultivos;
        this.estadosParcela = estadosParcela;
        this.parcelasCount = parcelasCount;
        this.totalHectareas = totalHectareas;
        this.hasActiveAlerts = hasActiveAlerts;
        this.activeAlertsCount = activeAlertsCount;
        this.maxAlertSeverity = maxAlertSeverity;
        this.totalOrders = totalOrders;
        this.lastOrderDate = lastOrderDate;
    }
}
