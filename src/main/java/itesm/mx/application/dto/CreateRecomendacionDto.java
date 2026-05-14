package itesm.mx.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateRecomendacionDto {
    @NotBlank(message = "titulo es requerido")
    @Size(max = 255, message = "titulo no puede exceder 255 caracteres")
    public String titulo;

    @Size(max = 1000, message = "descripcion no puede exceder 1000 caracteres")
    public String descripcion;

    @NotBlank(message = "tipoPlaga es requerido")
    @Size(max = 100, message = "tipoPlaga no puede exceder 100 caracteres")
    public String tipoPlaga;

    @NotBlank(message = "productosRecomendados es requerido")
    @Size(max = 500, message = "productosRecomendados no puede exceder 500 caracteres")
    public String productosRecomendados;
}
