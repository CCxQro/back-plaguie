package itesm.mx.application.dto;

public class GetLocationResponseDto {
    public Long locationId;
    public Double latitude;
    public Double longitude;
    public Long stateId;
    public String stateName;
    public Long municipalityId;
    public String municipalityName;
    public Long localityId;
    public String localityName;
    public Long propertyId;
    public String propertyName;

    public GetLocationResponseDto() {
    }

    public GetLocationResponseDto(
            Long locationId,
            Double latitude,
            Double longitude,
            Long stateId,
            String stateName,
            Long municipalityId,
            String municipalityName,
            Long localityId,
            String localityName,
            Long propertyId,
            String propertyName
    ) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stateId = stateId;
        this.stateName = stateName;
        this.municipalityId = municipalityId;
        this.municipalityName = municipalityName;
        this.localityId = localityId;
        this.localityName = localityName;
        this.propertyId = propertyId;
        this.propertyName = propertyName;
    }
}
