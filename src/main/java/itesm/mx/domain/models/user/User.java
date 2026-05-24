package itesm.mx.domain.models.user;

import itesm.mx.domain.models.location.Location;

public class User {
    private Long userId;
    private String firebaseUuid;
    private String name;
    private String email;
    private Integer roleId;
    private Boolean isActive;
    private Location location;

    public User() {}

    public User(Long userId, String firebaseUuid, String name, String email, Integer roleId, Boolean isActive) {
        this.userId = userId;
        this.firebaseUuid = firebaseUuid;
        this.name = name;
        this.email = email;
        this.roleId = roleId;
        this.isActive = isActive;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirebaseUuid() { return firebaseUuid; }
    public void setFirebaseUuid(String firebaseUuid) { this.firebaseUuid = firebaseUuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
