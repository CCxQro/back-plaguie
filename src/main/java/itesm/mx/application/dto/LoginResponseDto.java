package itesm.mx.application.dto;

public class LoginResponseDto {
    public String name;
    public String email;
    public Integer roleId;
    public boolean isActive;
    
    public LoginResponseDto(String name, String email, Integer roleId,  boolean isActive) {
        this.name = name;
        this.email = email;
        this.roleId = roleId;
        this.isActive = isActive;
    }
}