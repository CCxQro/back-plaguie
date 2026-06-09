package itesm.mx.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BuyOrderDto {

    @NotNull(message = "Los items del pedido son requeridos")
    @Size(min = 1, message = "El pedido debe tener al menos un item")
    public List<@Valid BuyOrderItemDto> items;

    @NotNull(message = "La dirección de envío es requerida")
    @Valid
    public ShippingAddressDto shippingAddress;

    public static class ShippingAddressDto {

        @NotNull(message = "La calle es requerida")
        public String street;

        @NotNull(message = "La ciudad es requerida")
        public String city;

        @NotNull(message = "El estado es requerido")
        public String state;

        public Double latitude;
        public Double longitude;
    }
}
