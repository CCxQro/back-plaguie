package itesm.mx.application.dto;

public class GetUserResponseDto {
    public Long userId;
    public String firebaseUuid;
    public String name;
    public String email;
    public Integer roleId;
    public Boolean isActive;

    public GetUserResponseDto() {}

    public GetUserResponseDto(Long userId, String firebaseUuid, String name, String email, Integer roleId, Boolean isActive) {
        this.userId = userId;
        this.firebaseUuid = firebaseUuid;
        this.name = name;
        this.email = email;
        this.roleId = roleId;
        this.isActive = isActive;
    }
}
