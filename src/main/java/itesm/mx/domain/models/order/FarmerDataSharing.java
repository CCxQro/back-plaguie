package itesm.mx.domain.models.order;

import java.time.LocalDateTime;

public class FarmerDataSharing {

    private Long id;
    private Long orderId;
    private Long farmerId;
    private Long providerId;
    private LocalDateTime sharedAt;
    private String snapshotJson;

    public FarmerDataSharing() {}

    public FarmerDataSharing(Long id, Long orderId, Long farmerId, Long providerId,
                              LocalDateTime sharedAt, String snapshotJson) {
        this.id = id;
        this.orderId = orderId;
        this.farmerId = farmerId;
        this.providerId = providerId;
        this.sharedAt = sharedAt;
        this.snapshotJson = snapshotJson;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }

    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
}
