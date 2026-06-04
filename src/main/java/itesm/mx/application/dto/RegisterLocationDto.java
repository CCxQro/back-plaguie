package itesm.mx.application.dto;

import jakarta.validation.constraints.NotNull;

public class RegisterLocationDto {

    @NotNull(message = "La latitud de la ubicacion es requerida")
    public Double latitude;

    @NotNull(message = "La longitud de la ubicacion es requerida")
    public Double longitude;

    public String stateName;
    public String municipalityName;
    public String localityName;
    public String propertyName;
}
