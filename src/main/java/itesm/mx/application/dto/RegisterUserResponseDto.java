package itesm.mx.application.dto;

public class RegisterUserResponseDto {
    public Long userId;
    public String firebaseUuid;
    public String name;
    public String email;
    public Integer roleId;
    public String firebaseToken;
    public Boolean isActive;
    public GetLocationResponseDto location;

    public RegisterUserResponseDto(Long userId, String firebaseUuid, String name, String email, Integer roleId, String firebaseToken) {
        this.userId = userId;
        this.firebaseUuid = firebaseUuid;
        this.name = name;
        this.email = email;
        this.roleId = roleId;
        this.firebaseToken = firebaseToken;
    }
}