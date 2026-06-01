package itesm.mx.domain.repository.parcela;

import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.SistemaRiego;
import itesm.mx.domain.models.parcela.TipoCultivo;

import java.util.List;

/**
 * Read access to the catalog tables referenced by a parcela
 * (estado_parcela, tipo_cultivo, sistema_riego): existence checks used
 * before persisting, plus list-all for populating the register-farm form.
 * The parcela FKs use NO_CONSTRAINT joins, so these IDs must be validated.
 */
public interface ParcelaCatalogRepository {
    boolean estadoParcelaExists(Long estadoParcelaId);
    boolean tipoCultivoExists(Long tipoCultivoId);
    boolean sistemaRiegoExists(Long sistemaRiegoId);

    List<EstadoParcela> findAllEstadosParcela();
    List<TipoCultivo> findAllTiposCultivo();
    List<SistemaRiego> findAllSistemasRiego();
}
