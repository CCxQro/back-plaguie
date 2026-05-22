package itesm.mx.application.usecase.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.RegisterLocationDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.AdministratorRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import itesm.mx.domain.repository.user.UserRepository;

import java.util.Optional;

@ApplicationScoped
public class UpdateUserUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    FarmerRepository farmerRepository;

    @Inject
    TechnicalSellerRepository technicalSellerRepository;

    @Inject
    AdministratorRepository administratorRepository;

    @Inject
    RegisterLocationUseCase registerLocationUseCase;

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

        Integer currentRoleId = existing.getRoleId();
        Integer newRoleId = updateUserDto.roleId;
        boolean roleChanged = newRoleId != null && !newRoleId.equals(currentRoleId);

        if (roleChanged && RoleConstants.FARMER.equals(currentRoleId)) {
            throw new IllegalArgumentException("No se puede cambiar el rol de un Agricultor");
        }

        if (roleChanged) {
            deactivateRoleProfile(currentRoleId, userId);
            activateRoleProfile(newRoleId, userId, updateUserDto.location);
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

    private void deactivateRoleProfile(Integer roleId, Long userId) {
        if (RoleConstants.ADMIN.equals(roleId)) {
            administratorRepository.findByIdUser(userId).ifPresent(admin -> {
                admin.setActive(false);
                administratorRepository.update(admin);
            });
        } else if (RoleConstants.SELLER.equals(roleId)) {
            technicalSellerRepository.findByIdUser(userId).ifPresent(seller -> {
                seller.setActive(false);
                technicalSellerRepository.update(seller);
            });
        } else if (RoleConstants.FARMER.equals(roleId)) {
            farmerRepository.findByIdUser(userId).ifPresent(farmer -> {
                farmer.setActive(false);
                farmerRepository.update(farmer);
            });
        }
    }

    private void activateRoleProfile(Integer roleId, Long userId, RegisterLocationDto locationDto) {
        if (RoleConstants.ADMIN.equals(roleId)) {
            Optional<Administrator> existing = administratorRepository.findByIdUser(userId);
            if (existing.isPresent()) {
                Administrator admin = existing.get();
                admin.setActive(true);
                administratorRepository.update(admin);
            } else {
                administratorRepository.save(new Administrator(null, userReference(userId), true));
            }
        } else if (RoleConstants.SELLER.equals(roleId)) {
            Optional<TechnicalSeller> existing = technicalSellerRepository.findByIdUser(userId);
            if (existing.isPresent()) {
                TechnicalSeller seller = existing.get();
                seller.setActive(true);
                technicalSellerRepository.update(seller);
            } else {
                technicalSellerRepository.save(
                        new TechnicalSeller(null, userReference(userId), resolveLocation(locationDto), true));
            }
        } else if (RoleConstants.FARMER.equals(roleId)) {
            Optional<Farmer> existing = farmerRepository.findByIdUser(userId);
            if (existing.isPresent()) {
                Farmer farmer = existing.get();
                farmer.setActive(true);
                farmerRepository.update(farmer);
            } else {
                farmerRepository.save(
                        new Farmer(null, userReference(userId), resolveLocation(locationDto), true));
            }
        }
    }

    private Location resolveLocation(RegisterLocationDto locationDto) {
        if (locationDto == null) {
            throw new IllegalArgumentException(
                    "Se requiere la ubicación para asignar este rol al usuario por primera vez");
        }
        GetLocationResponseDto locationResponse = registerLocationUseCase.execute(
                LocationDtoMapper.toLocationData(locationDto));
        Location location = new Location();
        location.setLocationId(locationResponse.locationId);
        return location;
    }

    private User userReference(Long userId) {
        User user = new User();
        user.setUserId(userId);
        return user;
    }
}