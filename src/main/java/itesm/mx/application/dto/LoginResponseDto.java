package itesm.mx.application.dto;

public class LoginResponseDto {
    public Long userId;
    public String name;
    public String email;
    public Integer roleId;
    public Boolean isActive;
    public GetLocationResponseDto location;

    public LoginResponseDto(Long userId, String name, String email, Integer roleId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.roleId = roleId;
    }
}