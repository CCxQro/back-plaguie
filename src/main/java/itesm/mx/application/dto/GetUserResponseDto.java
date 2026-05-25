package itesm.mx.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetUserResponseDto {
    public Long userId;
    public String firebaseUuid;
    public String name;
    public String email;
    public Integer roleId;
    public Boolean isActive;
    public GetLocationResponseDto location;

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
