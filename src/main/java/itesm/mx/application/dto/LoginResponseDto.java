package itesm.mx.application.dto;

public class LoginResponseDto {
    public String name;
    public String email;
    
    public LoginResponseDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}