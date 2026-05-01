package itesm.mx.domain.models.user;

import itesm.mx.domain.models.location.Location;

public class Farmer {
    private Long farmerId;
    private User user;
    private Location location;
    private Boolean isActive;

    public Farmer() {
    }

    public Farmer(Long farmerId, User user, Location location, Boolean isActive) {
        this.farmerId = farmerId;
        this.user = user;
        this.location = location;
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
