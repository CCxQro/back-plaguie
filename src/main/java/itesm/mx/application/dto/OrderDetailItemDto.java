package itesm.mx.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderDetailItemDto {

    @NotNull(message = "El id del producto es requerido")
    @Positive(message = "El id del producto debe ser positivo")
    public Long productId;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    public Integer quantity;

    @NotNull(message = "El precio unitario es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a cero")
    public Float unitPrice;
}
