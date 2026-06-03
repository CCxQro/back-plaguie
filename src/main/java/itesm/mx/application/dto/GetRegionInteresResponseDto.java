package itesm.mx.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** A seller's configured region of interest (HU-26). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRegionInteresResponseDto {

    public Long regionInteresId;
    public Long stateId;
    public String stateName;
    public String createdAt;

    public GetRegionInteresResponseDto() {
    }

    public GetRegionInteresResponseDto(Long regionInteresId, Long stateId,
                                        String stateName, String createdAt) {
        this.regionInteresId = regionInteresId;
        this.stateId = stateId;
        this.stateName = stateName;
        this.createdAt = createdAt;
    }
}
