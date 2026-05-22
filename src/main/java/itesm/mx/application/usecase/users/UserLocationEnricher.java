package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.application.usecase.users.subUsers.GetFarmerByUserIdUseCase;
import itesm.mx.application.usecase.users.subUsers.GetTechnicalSellerByUserIdUseCase;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.location.LocationRepository;

@ApplicationScoped
public class UserLocationEnricher {

    private static final Integer FARMER_ROLE_ID = 2;
    private static final Integer SELLER_ROLE_ID = 3;

    @Inject
    GetFarmerByUserIdUseCase getFarmerByUserIdUseCase;

    @Inject
    GetTechnicalSellerByUserIdUseCase getTechnicalSellerByUserIdUseCase;

    @Inject
    LocationRepository locationRepository;

    public void enrich(GetUserResponseDto dto, User user) {
        if (dto == null || user == null || user.getRoleId() == null) {
            return;
        }

        Long locationId = resolveLocationId(user);
        if (locationId == null) {
            return;
        }

        locationRepository.findLocationById(locationId)
                .map(LocationDtoMapper::toResponseDto)
                .ifPresent(locationDto -> dto.location = locationDto);
    }

    private Long resolveLocationId(User user) {
        if (FARMER_ROLE_ID.equals(user.getRoleId())) {
            return getFarmerByUserIdUseCase.execute(user.getUserId())
                    .map(Farmer::getLocation)
                    .map(Location::getLocationId)
                    .orElse(null);
        }
        if (SELLER_ROLE_ID.equals(user.getRoleId())) {
            return getTechnicalSellerByUserIdUseCase.execute(user.getUserId())
                    .map(TechnicalSeller::getLocation)
                    .map(Location::getLocationId)
                    .orElse(null);
        }
        return null;
    }
}
