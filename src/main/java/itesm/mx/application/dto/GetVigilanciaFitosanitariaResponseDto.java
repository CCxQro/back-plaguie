package itesm.mx.application.dto;

import java.math.BigDecimal;

public class GetVigilanciaFitosanitariaResponseDto {
    public Long vigilanciaFitosanitariaId;
    public Long systemMonitoringId;
    public String systemMonitoringName;
    public Long identificationKeyId;
    public String identificationKeyName;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public Long locationId;
    public Long plagueId;
    public String plagueName;
    public Long hostId;
    public String hostName;
    public Long varietyId;
    public String varietyName;
    public Long speciesId;
    public String speciesName;
    public BigDecimal ahosp;
    public Long statusId;
    public String statusName;
    public Long validatedByUserId;
    public String validatedAt;

    public GetVigilanciaFitosanitariaResponseDto() {
    }

    public GetVigilanciaFitosanitariaResponseDto(
            Long vigilanciaFitosanitariaId,
            Long systemMonitoringId,
            String systemMonitoringName,
            Long identificationKeyId,
            String identificationKeyName,
            BigDecimal latitude,
            BigDecimal longitude,
            Long locationId,
            Long plagueId,
            String plagueName,
            Long hostId,
            String hostName,
            Long varietyId,
            String varietyName,
            Long speciesId,
            String speciesName,
            BigDecimal ahosp,
            Long statusId,
            String statusName,
            Long validatedByUserId,
            String validatedAt
    ) {
        this.vigilanciaFitosanitariaId = vigilanciaFitosanitariaId;
        this.systemMonitoringId = systemMonitoringId;
        this.systemMonitoringName = systemMonitoringName;
        this.identificationKeyId = identificationKeyId;
        this.identificationKeyName = identificationKeyName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationId = locationId;
        this.plagueId = plagueId;
        this.plagueName = plagueName;
        this.hostId = hostId;
        this.hostName = hostName;
        this.varietyId = varietyId;
        this.varietyName = varietyName;
        this.speciesId = speciesId;
        this.speciesName = speciesName;
        this.ahosp = ahosp;
        this.statusId = statusId;
        this.statusName = statusName;
        this.validatedByUserId = validatedByUserId;
        this.validatedAt = validatedAt;
    }
}