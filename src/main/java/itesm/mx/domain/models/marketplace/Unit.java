package itesm.mx.domain.models.marketplace;

import itesm.mx.domain.models.user.User;

public class Unit {
    private Long unitId;
    private User user;
    private String name;
    private Status status;

    public Unit() {}

    public Unit(Long unitId, User user, String name, Status status) {
        this.unitId = unitId;
        this.user = user;
        this.name = name;
        this.status = status;
    }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}