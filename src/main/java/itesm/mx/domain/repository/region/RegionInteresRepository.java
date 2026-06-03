package itesm.mx.domain.repository.region;

import itesm.mx.domain.models.region.RegionInteres;

import java.util.List;
import java.util.Optional;

public interface RegionInteresRepository {

    /** All regions of interest configured by a given seller (by user id). */
    List<RegionInteres> findByUserId(Long userId);

    /** A single region of interest by id, scoped to its owner. */
    Optional<RegionInteres> findByIdAndUserId(Long regionInteresId, Long userId);

    /** True if the seller already has this state configured as a region of interest. */
    boolean existsByUserIdAndStateId(Long userId, Long stateId);

    RegionInteres save(RegionInteres regionInteres);

    /** Deletes a region of interest owned by the user. Returns true if a row was removed. */
    boolean deleteByIdAndUserId(Long regionInteresId, Long userId);
}
