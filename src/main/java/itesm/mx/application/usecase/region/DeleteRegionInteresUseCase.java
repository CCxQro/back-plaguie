package itesm.mx.application.usecase.region;

import itesm.mx.domain.repository.region.RegionInteresRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Removes a region of interest owned by the authenticated seller (HU-26 CA-03). */
@ApplicationScoped
public class DeleteRegionInteresUseCase {

    @Inject
    RegionInteresRepository regionInteresRepository;

    public void execute(Long userId, Long regionInteresId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }
        if (regionInteresId == null || regionInteresId <= 0) {
            throw new IllegalArgumentException("El ID de la región de interés no es válido");
        }

        boolean removed = regionInteresRepository.deleteByIdAndUserId(regionInteresId, userId);
        if (!removed) {
            throw new IllegalStateException(
                    "Región de interés no encontrada o no pertenece al usuario");
        }
    }
}
