package itesm.mx.application.dto;

public class FarmerLocationDto {
    public Long farmerId;
    public String farmerName;
    public Long orderId;
    public Double latitude;
    public Double longitude;
    public Long locationId;

    public FarmerLocationDto() {}

    public FarmerLocationDto(Long farmerId, String farmerName, Long orderId,
                              Double latitude, Double longitude, Long locationId) {
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationId = locationId;
    }
}
