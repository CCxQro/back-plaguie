package itesm.mx.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class RegisterParcelaDto {

    // Human-readable identifier/name of the parcela (nombre_parcela).
    @NotBlank(message = "El nombre de la parcela es requerido")
    public String nombreParcela;

    @Positive(message = "El tamano en hectareas debe ser mayor a cero")
    public Double tamanoHectareas;

    public LocalDate fechaSiembra;

    public LocalDate fechaCosecha;

    @DecimalMin(value = "0", message = "El pH del suelo debe estar entre 0 y 14")
    @DecimalMax(value = "14", message = "El pH del suelo debe estar entre 0 y 14")
    public Double phSuelo;

    @NotNull(message = "La ubicacion es requerida")
    @Valid
    public RegisterLocationDto ubicacion;

    @NotNull(message = "El estado de la parcela es requerido")
    @Positive(message = "El identificador del estado de la parcela debe ser positivo")
    public Long estadoParcelaId;

    @NotNull(message = "El tipo de cultivo es requerido")
    @Positive(message = "El identificador del tipo de cultivo debe ser positivo")
    public Long tipoCultivoId;

    @NotNull(message = "El sistema de riego es requerido")
    @Positive(message = "El identificador del sistema de riego debe ser positivo")
    public Long sistemaRiegoId;
}
