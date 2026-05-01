package itesm.mx.domain.models.user;

import itesm.mx.domain.models.location.Location;

public class TechnicalSeller {
    private Long technicalSellerId;
    private User user;
    private Location location;
    private Boolean isActive;

    public TechnicalSeller() {
    }

    public TechnicalSeller(Long technicalSellerId, User user, Location location, Boolean isActive) {
        this.technicalSellerId = technicalSellerId;
        this.user = user;
        this.location = location;
        this.isActive = isActive;
    }

    public Long getTechnicalSellerId() {
        return technicalSellerId;
    }

    public void setTechnicalSellerId(Long technicalSellerId) {
        this.technicalSellerId = technicalSellerId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
