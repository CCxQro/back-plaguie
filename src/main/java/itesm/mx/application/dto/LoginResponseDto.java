package itesm.mx.application.dto;

public class LoginResponseDto {
    public String token; 
    public String name;
    public String email;
    
    public LoginResponseDto(String token, String name, String email) {
        this.token = token;
        this.name = name;
        this.email = email;
    }
}
