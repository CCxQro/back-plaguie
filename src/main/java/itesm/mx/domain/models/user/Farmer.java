package itesm.mx.domain.models.user;

public class Farmer {
    private Long farmerId;
    private User user;
    private Boolean isActive;
    /** Account approval status (see {@link AccountStatusConstants}): Accepted / Revision / Rejected. */
    private Long statusId;
    private String statusName;

    public Farmer() {
    }

    public Farmer(Long farmerId, User user, Boolean isActive) {
        this.farmerId = farmerId;
        this.user = user;
        this.isActive = isActive;
    }

    public Farmer(Long farmerId, User user, Boolean isActive, Long statusId) {
        this.farmerId = farmerId;
        this.user = user;
        this.isActive = isActive;
        this.statusId = statusId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
