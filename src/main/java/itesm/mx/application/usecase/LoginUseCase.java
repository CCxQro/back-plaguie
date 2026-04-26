package itesm.mx.application.usecase;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;

@ApplicationScoped
public class LoginUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    Instance<FirebaseTokenVerifier> firebaseTokenVerifierInstance;

    public LoginResponseDto execute(LoginDto loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (loginDto.firebaseToken == null || loginDto.firebaseToken.isBlank()) {
            throw new IllegalArgumentException("Se requiere un token de Firebase válido para iniciar sesión");
        }
        String firebaseUuid = verifyFirebaseToken(loginDto.firebaseToken);

        User user = userRepository.findByFirebaseUuid(firebaseUuid)
                .orElseThrow(() -> new SecurityException("Usuario no encontrado en la base de datos con este UUID de Firebase"));
        return new LoginResponseDto(user.getName(), user.getEmail(), user.getRoleId());
    }

    /**
     * Verifies a Firebase token and extracts the UID.
     * @param token the Firebase ID token
     * @return the Firebase UID
     * @throws IllegalArgumentException if the token is blank/invalid format
     * @throws SecurityException if token verification fails
     */
    private String verifyFirebaseToken(String token) {
        if (firebaseTokenVerifierInstance != null && firebaseTokenVerifierInstance.isResolvable()) {
            try {
                return firebaseTokenVerifierInstance.get().verifyTokenAndGetUid(token);
            } catch (FirebaseAuthException e) {
                throw new SecurityException("La verificación del token de Firebase falló: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        throw new SecurityException("No hay un verificador de Firebase configurado en el entorno actual");
    }
}