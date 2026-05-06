package itesm.mx.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreateVigilanciaFitosanitariaDto {
    @NotNull(message = "systemMonitoringId es requerido")
    @Positive(message = "systemMonitoringId debe ser mayor a 0")
    public Long systemMonitoringId;

    @NotNull(message = "identificationKeyId es requerido")
    @Positive(message = "identificationKeyId debe ser mayor a 0")
    public Long identificationKeyId;

    @NotNull(message = "latitude es requerido")
    @DecimalMin(value = "-90.0", message = "latitude debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "latitude debe estar entre -90 y 90")
    @Digits(integer = 2, fraction = 8, message = "latitude debe tener maximo 8 decimales")
    public BigDecimal latitude;

    @NotNull(message = "longitude es requerido")
    @DecimalMin(value = "-180.0", message = "longitude debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "longitude debe estar entre -180 y 180")
    @Digits(integer = 3, fraction = 8, message = "longitude debe tener maximo 8 decimales")
    public BigDecimal longitude;

    @NotNull(message = "locationId es requerido")
    @Positive(message = "locationId debe ser mayor a 0")
    public Long locationId;

    @NotNull(message = "plagueId es requerido")
    @Positive(message = "plagueId debe ser mayor a 0")
    public Long plagueId;

    @NotNull(message = "hostId es requerido")
    @Positive(message = "hostId debe ser mayor a 0")
    public Long hostId;

    @NotNull(message = "varietyId es requerido")
    @Positive(message = "varietyId debe ser mayor a 0")
    public Long varietyId;

    @NotNull(message = "speciesId es requerido")
    @Positive(message = "speciesId debe ser mayor a 0")
    public Long speciesId;

    @DecimalMin(value = "0.0", message = "ahosp no puede ser negativo")
    @Digits(integer = 3, fraction = 2, message = "ahosp debe tener formato decimal(5,2)")
    public BigDecimal ahosp;
}