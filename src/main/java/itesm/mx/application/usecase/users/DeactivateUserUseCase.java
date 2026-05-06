package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.repository.user.UserRepository;

@ApplicationScoped
public class DeactivateUserUseCase {

    @Inject
    UserRepository userRepository;

    @Transactional
    public void execute(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }

        userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado con id: " + userId));

        userRepository.deactivate(userId);
    }
}
