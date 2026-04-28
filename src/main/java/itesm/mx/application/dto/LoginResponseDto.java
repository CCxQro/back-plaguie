package itesm.mx.application.dto;

public class LoginResponseDto {
    public String name;
    public String email;
    public Integer roleId;
    
    public LoginResponseDto(String name, String email, Integer roleId) {
        this.name = name;
        this.email = email;
        this.roleId = roleId;
    }
}