package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;

@ApplicationScoped
public class GetUserByIdUseCase {

    @Inject
    UserRepository userRepository;

    public GetUserResponseDto execute(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado con id: " + userId));

        return new GetUserResponseDto(
                user.getUserId(),
                user.getFirebaseUuid(),
                user.getName(),
                user.getEmail(),
                user.getRoleId(),
                user.getActive()
        );
    }
}
