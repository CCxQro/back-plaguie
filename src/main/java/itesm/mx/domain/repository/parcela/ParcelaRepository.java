package itesm.mx.domain.repository.parcela;

import itesm.mx.domain.models.parcela.Parcela;

import java.util.List;
import java.util.Optional;

public interface ParcelaRepository {
    List<Parcela> findAllParcelas();
    Optional<Parcela> findParcelaById(Long parcelaId);
    List<Parcela> findByFarmerId(Long farmerId);
    Parcela save(Parcela parcela);
    Parcela update(Parcela parcela);
    void delete(Long parcelaId);
    void setActiveByFarmerId(Long farmerId, boolean isActive);
}
