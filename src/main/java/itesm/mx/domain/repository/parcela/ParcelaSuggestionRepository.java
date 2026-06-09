package itesm.mx.domain.repository.parcela;

import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import java.util.List;

public interface ParcelaSuggestionRepository {
    List<ParcelaSuggestion> findByParcelaId(Long parcelaId);
    ParcelaSuggestion save(ParcelaSuggestion suggestion);
}
