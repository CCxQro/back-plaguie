package itesm.mx.application.dto;

import jakarta.validation.constraints.NotNull;

/** Request body to add a region of interest for the authenticated seller (HU-26). */
public class CreateRegionInteresDto {

    @NotNull(message = "El id del estado es requerido")
    public Long stateId;

    public CreateRegionInteresDto() {
    }

    public CreateRegionInteresDto(Long stateId) {
        this.stateId = stateId;
    }
}
