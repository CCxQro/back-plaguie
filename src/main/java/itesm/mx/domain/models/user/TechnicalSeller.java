package itesm.mx.domain.models.user;

public class TechnicalSeller {
    private Long technicalSellerId;
    private User user;
    private Boolean isActive;

    public TechnicalSeller() {
    }

    public TechnicalSeller(Long technicalSellerId, User user, Boolean isActive) {
        this.technicalSellerId = technicalSellerId;
        this.user = user;
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
