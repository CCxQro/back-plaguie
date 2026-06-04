package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.ParcelaDetailDto;
import itesm.mx.application.mapper.parcela.ParcelaAppMapper;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.parcela.ParcelaSuggestionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetParcelaDetailUseCase {

    @Inject
    ParcelaRepository parcelaRepository;

    @Inject
    ParcelaSuggestionRepository parcelaSuggestionRepository;

    public ParcelaDetailDto execute(Long parcelaId, Long farmerId) {
        if (parcelaId == null) {
            throw new IllegalArgumentException("ID de parcela inválido");
        }
        
        Parcela parcela = parcelaRepository.findParcelaById(parcelaId)
                .orElseThrow(() -> new IllegalArgumentException("Parcela no encontrada"));
                
        if (farmerId != null && !parcela.getFarmer().getFarmerId().equals(farmerId)) {
            throw new IllegalArgumentException("No tienes permiso para ver esta parcela");
        }

        List<ParcelaSuggestion> suggestions = parcelaSuggestionRepository.findByParcelaId(parcelaId);

        // Simple mock calculation for health %: 100 - (number of suggestions * 10)
        double health = 100.0 - (suggestions.size() * 10.0);
        if (health < 0) health = 0;

        ParcelaDetailDto dto = new ParcelaDetailDto();
        dto.parcela = ParcelaAppMapper.toResponseDto(parcela);
        dto.healthPercentage = health;
        dto.suggestions = suggestions.stream().map(ParcelaSuggestion::getMessage).collect(Collectors.toList());

        return dto;
    }
}
