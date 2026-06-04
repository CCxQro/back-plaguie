package itesm.mx.infrastructure.mapper.parcela;

import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.infrastructure.mapper.alerta.AlertaMapper;
import itesm.mx.infrastructure.persistence.entity.parcela.ParcelaSuggestionEntity;

public class ParcelaSuggestionMapper {
    private ParcelaSuggestionMapper() {}

    public static ParcelaSuggestion toDomain(ParcelaSuggestionEntity entity) {
        if (entity == null) return null;
        ParcelaSuggestion domain = new ParcelaSuggestion();
        domain.setSuggestionId(entity.suggestionId);
        domain.setMessage(entity.message);
        domain.setCreatedAt(entity.createdAt);
        domain.setParcela(ParcelaMapper.toDomain(entity.parcela));
        if (entity.alerta != null) {
            domain.setAlerta(AlertaMapper.toDomain(entity.alerta));
        }
        return domain;
    }

    public static ParcelaSuggestionEntity toEntity(ParcelaSuggestion domain) {
        if (domain == null) return null;
        ParcelaSuggestionEntity entity = new ParcelaSuggestionEntity();
        entity.suggestionId = domain.getSuggestionId();
        entity.message = domain.getMessage();
        entity.createdAt = domain.getCreatedAt();
        entity.parcela = ParcelaMapper.toEntity(domain.getParcela());
        if (domain.getAlerta() != null) {
            entity.alerta = AlertaMapper.toEntity(domain.getAlerta());
        }
        return entity;
    }
}
