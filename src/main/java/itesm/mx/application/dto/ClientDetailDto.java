package itesm.mx.application.dto;

import java.util.List;

public class ClientDetailDto {
    public Long farmerId;
    public Long userId;
    public String name;
    public String email;
    public Boolean isActive;
    public Long locationId;
    public Double latitude;
    public Double longitude;
    public String state;
    public String municipality;
    public String locality;
    public String property;
    public List<ClientParcelaSummaryDto> parcelas;
    public List<ClientAlertaSummaryDto> alertas;
    public ClientOrderSummaryDto orderSummary;

    public ClientDetailDto() {}

    public ClientDetailDto(Long farmerId, Long userId, String name, String email, Boolean isActive,
                           Long locationId, Double latitude, Double longitude,
                           String state, String municipality, String locality, String property,
                           List<ClientParcelaSummaryDto> parcelas,
                           List<ClientAlertaSummaryDto> alertas,
                           ClientOrderSummaryDto orderSummary) {
        this.farmerId = farmerId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.municipality = municipality;
        this.locality = locality;
        this.property = property;
        this.parcelas = parcelas;
        this.alertas = alertas;
        this.orderSummary = orderSummary;
    }
}
