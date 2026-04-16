package itesm.mx.application.usecase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;

@ApplicationScoped
public class LoginUseCase {

    @Inject
    UserRepository userRepository;

    public LoginResponseDto execute(LoginDto loginDto) {
        User user = null;

        if (loginDto.firebaseToken != null && !loginDto.firebaseToken.isEmpty()) {
            // TODO: Verify the Firebase token and get the UUID.
            // String firebaseUuid = firebaseService.verifyToken(loginDto.firebaseToken);
            String firebaseUuid = "mock-uuid-firebase";
            
            user = userRepository.findByFirebaseUuid(firebaseUuid)
                    .orElseThrow(() -> new RuntimeException("User not found in the database with this Firebase UUID"));
        } else {
            user = userRepository.findByEmail(loginDto.email)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!user.getPassword().equals(loginDto.password)) {
                throw new RuntimeException("Invalid credentials");
            }
        }

        String generatedToken = "mock-jwt-token-interno";

        return new LoginResponseDto(generatedToken, user.getName(), user.getEmail());
    }
}
