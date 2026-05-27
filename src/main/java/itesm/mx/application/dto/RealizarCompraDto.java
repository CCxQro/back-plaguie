package itesm.mx.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class RealizarCompraDto {

    @NotNull(message = "Los productos de la compra son requeridos")
    @Size(min = 1, message = "La compra debe tener al menos un producto")
    public List<@Valid RealizarCompraItemDto> items;
}
