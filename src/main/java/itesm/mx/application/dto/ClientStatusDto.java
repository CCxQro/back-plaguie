package itesm.mx.application.dto;

import java.util.List;

public class ClientStatusDto {
    public Long farmerId;
    public List<ClientParcelaSummaryDto> parcelas;
    public List<ClientAlertaSummaryDto> recentActiveAlerts;

    public ClientStatusDto() {}

    public ClientStatusDto(Long farmerId, List<ClientParcelaSummaryDto> parcelas, List<ClientAlertaSummaryDto> recentActiveAlerts) {
        this.farmerId = farmerId;
        this.parcelas = parcelas;
        this.recentActiveAlerts = recentActiveAlerts;
    }
}
