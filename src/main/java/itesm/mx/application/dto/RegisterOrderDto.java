package itesm.mx.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class RegisterOrderDto {

    @NotNull(message = "El id del agricultor es requerido")
    @Positive(message = "El id del agricultor debe ser positivo")
    public Long farmerId;

    @NotNull(message = "El id del vendedor es requerido")
    @Positive(message = "El id del vendedor debe ser positivo")
    public Long sellerId;

    @NotNull(message = "El estado del pedido es requerido")
    @Positive(message = "El id del estado debe ser positivo")
    public Long orderStatusId;

    @NotNull(message = "El monto total es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto total debe ser mayor a cero")
    public BigDecimal totalAmount;

    @NotNull(message = "Los detalles del pedido son requeridos")
    @Size(min = 1, message = "El pedido debe tener al menos un detalle")
    public List<@Valid OrderDetailItemDto> details;
}
