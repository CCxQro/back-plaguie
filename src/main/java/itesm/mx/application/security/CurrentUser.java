package itesm.mx.application.security;

public class CurrentUser {
    private final Long userId;
    private final String firebaseUuid;
    private final String name;
    private final String email;
    private final Integer roleId;

    public CurrentUser(Long userId, String firebaseUuid, String name, String email, Integer roleId) {
        this.userId = userId;
        this.firebaseUuid = firebaseUuid;
        this.name = name;
        this.email = email;
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFirebaseUuid() {
        return firebaseUuid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getRoleId() {
        return roleId;
    }
}
