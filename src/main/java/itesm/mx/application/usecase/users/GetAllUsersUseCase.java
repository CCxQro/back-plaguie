package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.domain.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetAllUsersUseCase {

    @Inject
    UserRepository userRepository;

    public List<GetUserResponseDto> execute() {
        return userRepository.findAllUsers().stream()
                .map(user -> new GetUserResponseDto(
                        user.getUserId(),
                        user.getFirebaseUuid(),
                        user.getName(),
                        user.getEmail(),
                        user.getRoleId(),
                        user.getActive()
                ))
                .collect(Collectors.toList());
    }
}
