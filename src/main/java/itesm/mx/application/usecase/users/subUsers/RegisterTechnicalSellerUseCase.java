package itesm.mx.application.usecase.users.subUsers;

import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegisterTechnicalSellerUseCase {

    @Inject
    TechnicalSellerRepository technicalSellerRepository;

    @Transactional
    public TechnicalSeller execute(TechnicalSeller technicalSeller) {
        if (technicalSeller == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (technicalSeller.getUser() == null || technicalSeller.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Se requiere el id_usuario del tecnico vendedor");
        }
        if (technicalSeller.getLocation() == null || technicalSeller.getLocation().getLocationId() == null) {
            throw new IllegalArgumentException("Se requiere el id_ubicacion del tecnico vendedor");
        }

        return technicalSellerRepository.save(technicalSeller);
    }
}

