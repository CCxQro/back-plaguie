package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.location.LocationRepository;

@ApplicationScoped
public class UserLocationEnricher {

    @Inject
    LocationRepository locationRepository;

    public void enrich(GetUserResponseDto dto, User user) {
        if (dto == null || user == null || user.getLocation() == null) {
            return;
        }

        Long locationId = user.getLocation().getLocationId();
        if (locationId == null) {
            return;
        }

        locationRepository.findLocationById(locationId)
                .map(LocationDtoMapper::toResponseDto)
                .ifPresent(locationDto -> dto.location = locationDto);
    }
}
