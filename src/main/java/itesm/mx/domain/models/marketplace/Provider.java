package itesm.mx.domain.models.marketplace;

import itesm.mx.domain.models.user.User;

public class Provider {
    private Long providerId;
    private User user;
    private String name;

    public Provider() {}

    public Provider(Long providerId, User user, String name) {
        this.providerId = providerId;
        this.user = user;
        this.name = name;
    }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}