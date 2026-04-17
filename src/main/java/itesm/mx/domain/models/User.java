package itesm.mx.domain.models;

public class User {
    private Long userId;
    private String firebaseUuid;
    private String name;
    private String email;
    private Integer roleId;

    public User() {}

    public User(Long userId, String firebaseUuid, String name, String email, Integer roleId) {
        this.userId = userId;
        this.firebaseUuid = firebaseUuid;
        this.name = name;
        this.email = email;
        this.roleId = roleId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirebaseUuid() { return firebaseUuid; }
    public void setFirebaseUuid(String firebaseUuid) { this.firebaseUuid = firebaseUuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
}
