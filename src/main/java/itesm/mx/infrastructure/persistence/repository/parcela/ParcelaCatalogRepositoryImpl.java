package itesm.mx.infrastructure.persistence.repository.parcela;

import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.SistemaRiego;
import itesm.mx.domain.models.parcela.TipoCultivo;
import itesm.mx.domain.repository.parcela.ParcelaCatalogRepository;
import itesm.mx.infrastructure.persistence.entity.parcela.EstadoParcelaEntity;
import itesm.mx.infrastructure.persistence.entity.parcela.SistemaRiegoEntity;
import itesm.mx.infrastructure.persistence.entity.parcela.TipoCultivoEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class ParcelaCatalogRepositoryImpl implements ParcelaCatalogRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public boolean estadoParcelaExists(Long estadoParcelaId) {
        return estadoParcelaId != null
                && entityManager.find(EstadoParcelaEntity.class, estadoParcelaId) != null;
    }

    @Override
    public boolean tipoCultivoExists(Long tipoCultivoId) {
        return tipoCultivoId != null
                && entityManager.find(TipoCultivoEntity.class, tipoCultivoId) != null;
    }

    @Override
    public boolean sistemaRiegoExists(Long sistemaRiegoId) {
        return sistemaRiegoId != null
                && entityManager.find(SistemaRiegoEntity.class, sistemaRiegoId) != null;
    }

    @Override
    public List<EstadoParcela> findAllEstadosParcela() {
        return entityManager
                .createQuery("select e from EstadoParcelaEntity e order by e.estadoParcelaId", EstadoParcelaEntity.class)
                .getResultList()
                .stream()
                .map(e -> new EstadoParcela(e.estadoParcelaId, e.nombre))
                .toList();
    }

    @Override
    public List<TipoCultivo> findAllTiposCultivo() {
        return entityManager
                .createQuery("select t from TipoCultivoEntity t order by t.tipoCultivoId", TipoCultivoEntity.class)
                .getResultList()
                .stream()
                .map(t -> new TipoCultivo(t.tipoCultivoId, t.nombre, t.fechaSiembra, t.fechaCosecha))
                .toList();
    }

    @Override
    public List<SistemaRiego> findAllSistemasRiego() {
        return entityManager
                .createQuery("select s from SistemaRiegoEntity s order by s.sistemaRiegoId", SistemaRiegoEntity.class)
                .getResultList()
                .stream()
                .map(s -> new SistemaRiego(s.sistemaRiegoId, s.nombre))
                .toList();
    }
}
