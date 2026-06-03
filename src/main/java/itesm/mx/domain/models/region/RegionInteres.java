package itesm.mx.domain.models.region;

import java.time.LocalDateTime;

/**
 * A seller's (technical seller) region of interest, used to scope early pest
 * alerts to the geographic areas the seller cares about. (HU-26)
 *
 * A region of interest is modeled as a reference to a {@code State} (estado)
 * owned by a specific seller user.
 */
public class RegionInteres {

    private Long regionInteresId;
    private Long userId;
    private Long stateId;
    private String stateName;
    private LocalDateTime createdAt;

    public RegionInteres() {
    }

    public RegionInteres(Long regionInteresId, Long userId, Long stateId,
                          String stateName, LocalDateTime createdAt) {
        this.regionInteresId = regionInteresId;
        this.userId = userId;
        this.stateId = stateId;
        this.stateName = stateName;
        this.createdAt = createdAt;
    }

    public Long getRegionInteresId() { return regionInteresId; }
    public void setRegionInteresId(Long regionInteresId) { this.regionInteresId = regionInteresId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getStateId() { return stateId; }
    public void setStateId(Long stateId) { this.stateId = stateId; }

    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
