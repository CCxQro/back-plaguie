package itesm.mx.domain.repository.reporte;

import itesm.mx.domain.models.reporte.PestMapPoint;

import java.util.List;

public interface PestMapRepository {

    /**
     * Validated (status = Accepted) phytosanitary surveillance observations with
     * coordinates, enriched with state/municipality, for the interactive pest map
     * (HU-27 CA-04). Only records that have coordinates are returned.
     */
    List<PestMapPoint> findValidatedMapPoints();
}
