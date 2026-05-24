package itesm.mx.domain.models.user;

public class Farmer {
    private Long farmerId;
    private User user;
    private Boolean isActive;

    public Farmer() {
    }

    public Farmer(Long farmerId, User user, Boolean isActive) {
        this.farmerId = farmerId;
        this.user = user;
        this.isActive = isActive;
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
}
