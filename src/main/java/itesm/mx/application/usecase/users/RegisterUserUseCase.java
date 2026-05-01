package itesm.mx.application.usecase.users;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.RegisterUserDto;
import itesm.mx.application.dto.RegisterUserResponseDto;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;

@ApplicationScoped
public class RegisterUserUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    FirebaseUserManager firebaseUserManager;

    @Transactional
    public RegisterUserResponseDto execute(RegisterUserDto registerUserDto) {
        if (registerUserDto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (registerUserDto.name == null || registerUserDto.name.isBlank()) {
            throw new IllegalArgumentException("Se requiere el nombre");
        }
        if (registerUserDto.email == null || registerUserDto.email.isBlank()) {
            throw new IllegalArgumentException("Se requiere el correo electrónico");
        }
        if (registerUserDto.password == null || registerUserDto.password.isBlank()) {
            throw new IllegalArgumentException("Se requiere la contraseña");
        }
        if (registerUserDto.roleId == null) {
            throw new IllegalArgumentException("Se requiere el rol del usuario");
        }

        userRepository.findByEmail(registerUserDto.email)
                .ifPresent(existingUser -> {
                    throw new IllegalStateException("Ya existe un usuario registrado con este correo electrónico");
                });

        String firebaseUuid;
        try {
            firebaseUuid = firebaseUserManager.createFirebaseUser(
                    registerUserDto.email,
                    registerUserDto.password,
                    registerUserDto.name
            );
        } catch (FirebaseAuthException e) {
            throw mapFirebaseCreateError(e);
        }

        User userToCreate = new User(
                null,
                firebaseUuid,
                registerUserDto.name,
                registerUserDto.email,
                registerUserDto.roleId,
                true
        );

        User createdUser;
        try {
            createdUser = userRepository.save(userToCreate);
        } catch (RuntimeException e) {
            rollbackFirebaseUser(firebaseUuid);
            throw new IllegalStateException(
                "No se pudo guardar el usuario en la base de datos. Verifica que el roleId exista y que no haya restricciones de integridad."
            );
        }

        String customToken;
        try {
            customToken = firebaseUserManager.generateCustomToken(firebaseUuid);
        } catch (FirebaseAuthException e) {
            rollbackFirebaseUser(firebaseUuid);
            throw new SecurityException("Error al generar token: " + e.getMessage());
        }

        return new RegisterUserResponseDto(
                createdUser.getUserId(),
                createdUser.getFirebaseUuid(),
                createdUser.getName(),
                createdUser.getEmail(),
                createdUser.getRoleId(),
                customToken
        );
    }

    private RuntimeException mapFirebaseCreateError(FirebaseAuthException e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toUpperCase();

        if (message.contains("EMAIL_EXISTS") || message.contains("EMAIL_ALREADY_EXISTS")) {
            return new IllegalStateException("El correo ya está registrado en Firebase");
        }
        if (message.contains("INVALID_PASSWORD") || message.contains("WEAK_PASSWORD")) {
            return new IllegalArgumentException("La contraseña no cumple con los requisitos de Firebase");
        }
        if (message.contains("INVALID_EMAIL")) {
            return new IllegalArgumentException("El correo electrónico no es válido para Firebase");
        }

        return new SecurityException("Error al crear el usuario en Firebase: " + e.getMessage());
    }

    private void rollbackFirebaseUser(String firebaseUuid) {
        try {
            firebaseUserManager.deleteFirebaseUser(firebaseUuid);
        } catch (FirebaseAuthException ignored) {
            // Preserve the original DB error while attempting best-effort cleanup.
        }
    }
}