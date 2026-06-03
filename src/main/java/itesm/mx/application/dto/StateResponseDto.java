package itesm.mx.application.dto;

/** State catalog item (id + nombre) for selectors, e.g. region-of-interest config (HU-26). */
public class StateResponseDto {

    public Long stateId;
    public String name;

    public StateResponseDto() {
    }

    public StateResponseDto(Long stateId, String name) {
        this.stateId = stateId;
        this.name = name;
    }
}
