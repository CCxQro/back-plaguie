package itesm.mx.application.usecase.location.location;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.repository.location.LocationRepository;

import java.util.List;

@ApplicationScoped
public class GetAllLocationsUseCase {

    @Inject
    LocationRepository locationRepository;

    public List<GetLocationResponseDto> execute() {
        return locationRepository.findAllLocations().stream()
                .map(LocationDtoMapper::toResponseDto)
                .toList();
    }
}
