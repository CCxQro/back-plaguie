package itesm.mx.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class UpdateVigilanciaFitosanitariaDto {
    @Positive(message = "systemMonitoringId debe ser mayor a 0")
    public Long systemMonitoringId;

    @Positive(message = "identificationKeyId debe ser mayor a 0")
    public Long identificationKeyId;

    @DecimalMin(value = "-90.0", message = "latitude debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "latitude debe estar entre -90 y 90")
    @Digits(integer = 2, fraction = 8, message = "latitude debe tener maximo 8 decimales")
    public BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "longitude debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "longitude debe estar entre -180 y 180")
    @Digits(integer = 3, fraction = 8, message = "longitude debe tener maximo 8 decimales")
    public BigDecimal longitude;

    @Positive(message = "locationId debe ser mayor a 0")
    public Long locationId;

    @Positive(message = "plagueId debe ser mayor a 0")
    public Long plagueId;

    @Positive(message = "hostId debe ser mayor a 0")
    public Long hostId;

    @Positive(message = "varietyId debe ser mayor a 0")
    public Long varietyId;

    @Positive(message = "speciesId debe ser mayor a 0")
    public Long speciesId;

    @DecimalMin(value = "0.0", message = "ahosp no puede ser negativo")
    @Digits(integer = 3, fraction = 2, message = "ahosp debe tener formato decimal(5,2)")
    public BigDecimal ahosp;
}