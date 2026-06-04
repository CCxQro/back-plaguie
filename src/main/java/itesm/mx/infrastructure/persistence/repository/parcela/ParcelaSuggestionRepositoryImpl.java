package itesm.mx.infrastructure.persistence.repository.parcela;

import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.domain.repository.parcela.ParcelaSuggestionRepository;
import itesm.mx.infrastructure.mapper.parcela.ParcelaSuggestionMapper;
import itesm.mx.infrastructure.persistence.entity.parcela.ParcelaSuggestionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ParcelaSuggestionRepositoryImpl implements ParcelaSuggestionRepository, PanacheRepositoryBase<ParcelaSuggestionEntity, Long> {

    @Override
    public List<ParcelaSuggestion> findByParcelaId(Long parcelaId) {
        return find("parcela.parcelaId = ?1 ORDER BY createdAt DESC", parcelaId)
                .stream()
                .map(ParcelaSuggestionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ParcelaSuggestion save(ParcelaSuggestion suggestion) {
        ParcelaSuggestionEntity entity = ParcelaSuggestionMapper.toEntity(suggestion);
        persist(entity);
        return ParcelaSuggestionMapper.toDomain(entity);
    }
}
