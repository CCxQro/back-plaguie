package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UserPageResponseDto;
import itesm.mx.domain.repository.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetAllUsersUseCase {

    @Inject
    UserRepository userRepository;

    public UserPageResponseDto execute(int page, int size, String name, Integer roleId, Boolean isActive) {
        if (page < 0) throw new IllegalArgumentException("El número de página no puede ser negativo");
        if (size <= 0) throw new IllegalArgumentException("El tamaño de página debe ser mayor a cero");

        List<GetUserResponseDto> content = userRepository.findUsersFiltered(page, size, name, roleId, isActive)
                .stream()
                .map(user -> new GetUserResponseDto(
                        user.getUserId(),
                        user.getFirebaseUuid(),
                        user.getName(),
                        user.getEmail(),
                        user.getRoleId(),
                        user.getActive()
                ))
                .collect(Collectors.toList());

        long total = userRepository.countUsersFiltered(name, roleId, isActive);
        return new UserPageResponseDto(content, total, page, size);
    }
}
