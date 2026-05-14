package itesm.mx.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateAlertaDto {
    @NotBlank(message = "titulo es requerido")
    @Size(max = 255, message = "titulo no puede exceder 255 caracteres")
    public String titulo;

    @Size(max = 1000, message = "descripcion no puede exceder 1000 caracteres")
    public String descripcion;

    @NotNull(message = "ubicacionId es requerido")
    @Positive(message = "ubicacionId debe ser mayor a 0")
    public Long ubicacionId;

    @NotBlank(message = "tipoPlaga es requerido")
    @Size(max = 100, message = "tipoPlaga no puede exceder 100 caracteres")
    public String tipoPlaga;

    @DecimalMin(value = "0.0", message = "hectareas no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "hectareas debe tener formato decimal(10,2)")
    public BigDecimal hectareas;

    @NotBlank(message = "severidad es requerido")
    @Pattern(regexp = "critico|advertencia|informacion", message = "severidad debe ser: critico, advertencia o informacion")
    public String severidad;
}
