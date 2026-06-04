package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UpdateSelfUserDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.application.usecase.location.location.UpdateLocationUseCase;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.LocationData;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.UserRepository;

@ApplicationScoped
public class UpdateSelfUserUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    RegisterLocationUseCase registerLocationUseCase;

    @Inject
    UpdateLocationUseCase updateLocationUseCase;

    @Inject
    UserLocationEnricher userLocationEnricher;

    @Transactional
    public GetUserResponseDto execute(Long userId, UpdateSelfUserDto dto) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (dto.name != null && dto.name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        User existing = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado con id: " + userId));

        Long existingLocationId = (existing.getLocation() != null)
                ? existing.getLocation().getLocationId()
                : null;

        User userToUpdate = new User();
        userToUpdate.setUserId(userId);
        userToUpdate.setName(dto.name);

        if (dto.location != null) {
            LocationData locationData = LocationDtoMapper.toLocationData(dto.location);
            if (existingLocationId != null) {
                updateLocationUseCase.execute(existingLocationId, locationData);
            } else {
                GetLocationResponseDto locationResponse = registerLocationUseCase.execute(locationData);
                Location location = new Location();
                location.setLocationId(locationResponse.locationId);
                userToUpdate.setLocation(location);
            }
        }

        User updated = userRepository.update(userToUpdate);

        GetUserResponseDto response = new GetUserResponseDto(
                updated.getUserId(),
                updated.getFirebaseUuid(),
                updated.getName(),
                updated.getEmail(),
                updated.getRoleId(),
                updated.getActive()
        );
        userLocationEnricher.enrich(response, updated);
        return response;
    }
}
