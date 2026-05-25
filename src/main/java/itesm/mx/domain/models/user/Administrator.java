package itesm.mx.domain.models.user;

public class Administrator {
    private Long administratorId;
    private User user;
    private Boolean isActive;

    public Administrator() {
    }

    public Administrator(Long administratorId, User user, Boolean isActive) {
        this.administratorId = administratorId;
        this.user = user;
        this.isActive = isActive;
    }

    public Long getAdministratorId() {
        return administratorId;
    }

    public void setAdministratorId(Long administratorId) {
        this.administratorId = administratorId;
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
