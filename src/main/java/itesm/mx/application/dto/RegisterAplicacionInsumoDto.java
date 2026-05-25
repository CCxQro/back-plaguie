package itesm.mx.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class RegisterAplicacionInsumoDto {

    @NotNull(message = "La fecha es requerida")
    public LocalDate fecha;

    @NotNull(message = "El identificador del producto es requerido")
    @Positive(message = "El identificador del producto debe ser positivo")
    public Long skuIdVendedor;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser positiva")
    public Double cantidad;

    @NotNull(message = "El identificador de la parcela es requerido")
    @Positive(message = "El identificador de la parcela debe ser positivo")
    public Long parcelaId;
}
