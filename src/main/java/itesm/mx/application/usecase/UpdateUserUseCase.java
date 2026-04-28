package itesm.mx.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;

@ApplicationScoped
public class UpdateUserUseCase {

    @Inject
    UserRepository userRepository;

    @Transactional
    public GetUserResponseDto execute(Long userId, UpdateUserDto updateUserDto) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }
        if (updateUserDto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (updateUserDto.name != null && updateUserDto.name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (updateUserDto.roleId != null && (updateUserDto.roleId < 1 || updateUserDto.roleId > 3)) {
            throw new IllegalArgumentException("El roleId debe ser 1 (Administrador), 2 (Agricultor) o 3 (Técnico Vendedor)");
        }

        User existing = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado con id: " + userId));

        if (Integer.valueOf(2).equals(existing.getRoleId()) && updateUserDto.roleId != null && !Integer.valueOf(2).equals(updateUserDto.roleId)) {
            throw new IllegalArgumentException("No se puede cambiar el rol de un Agricultor");
        }

        User userToUpdate = new User();
        userToUpdate.setUserId(userId);
        userToUpdate.setName(updateUserDto.name);
        userToUpdate.setRoleId(updateUserDto.roleId);
        userToUpdate.setActive(updateUserDto.isActive);

        User updated = userRepository.update(userToUpdate);

        return new GetUserResponseDto(
                updated.getUserId(),
                updated.getFirebaseUuid(),
                updated.getName(),
                updated.getEmail(),
                updated.getRoleId(),
                updated.getActive()
        );
    }
}
