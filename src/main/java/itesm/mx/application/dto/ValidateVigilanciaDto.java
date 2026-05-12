package itesm.mx.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ValidateVigilanciaDto {
    @NotNull(message = "statusId es requerido")
    @Positive(message = "statusId debe ser mayor a 0")
    public Long statusId;
}
