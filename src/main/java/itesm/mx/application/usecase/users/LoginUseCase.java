package itesm.mx.application.usecase.users;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.AdministratorRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import itesm.mx.domain.repository.user.UserRepository;
import itesm.mx.domain.repository.location.LocationRepository;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;

@ApplicationScoped
public class LoginUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    AdministratorRepository administratorRepository;

    @Inject
    FarmerRepository farmerRepository;

    @Inject
    TechnicalSellerRepository technicalSellerRepository;

    @Inject
    LocationRepository locationRepository;

    @Inject
    Instance<FirebaseTokenVerifier> firebaseTokenVerifierInstance;

    @Transactional
    public LoginResponseDto execute(LoginDto loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (loginDto.firebaseToken == null || loginDto.firebaseToken.isBlank()) {
            throw new IllegalArgumentException("Se requiere un token de Firebase válido para iniciar sesión");
        }
        String firebaseUuid = verifyFirebaseToken(loginDto.firebaseToken);

        User user = userRepository.findByFirebaseUuid(firebaseUuid)
                .orElseThrow(() -> new SecurityException("Usuario no encontrado en la base de datos con este UUID de Firebase"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new SecurityException("La cuenta de usuario está desactivada");
        }

        LoginResponseDto response = new LoginResponseDto(user.getUserId(), user.getName(), user.getEmail(), user.getRoleId());

        if (RoleConstants.ADMIN.equals(user.getRoleId())) {
            administratorRepository.findByIdUser(user.getUserId()).ifPresent(admin ->
                    response.isActive = admin.getActive()
            );
        } else if (RoleConstants.SELLER.equals(user.getRoleId())) {
            technicalSellerRepository.findByIdUser(user.getUserId()).ifPresent(seller -> {
                response.isActive = seller.getActive();
                response.location = resolveLocation(seller.getLocation());
            });
        } else if (RoleConstants.FARMER.equals(user.getRoleId())) {
            farmerRepository.findByIdUser(user.getUserId()).ifPresent(farmer -> {
                response.isActive = farmer.getActive();
                response.location = resolveLocation(farmer.getLocation());
            });
        }

        return response;
    }

    private GetLocationResponseDto resolveLocation(Location location) {
        if (location == null || location.getLocationId() == null) {
            return null;
        }
        return locationRepository.findLocationById(location.getLocationId())
                .map(LocationDtoMapper::toResponseDto)
                .orElse(null);
    }

    private String verifyFirebaseToken(String token) {
        if (firebaseTokenVerifierInstance != null && firebaseTokenVerifierInstance.isResolvable()) {
            try {
                return firebaseTokenVerifierInstance.get().verifyTokenAndGetUid(token);
            } catch (FirebaseAuthException e) {
                throw new SecurityException("La verificación del token de Firebase falló: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        throw new SecurityException("No hay un verificador de Firebase configurado en el entorno actual");
    }
}