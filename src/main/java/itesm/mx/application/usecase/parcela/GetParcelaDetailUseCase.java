package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.ParcelaDetailDto;
import itesm.mx.application.dto.ParcelaHistorialItemDto;
import itesm.mx.application.mapper.parcela.ParcelaAppMapper;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.domain.models.parcela.SistemaRiego;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.parcela.ParcelaSuggestionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
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

        // Cálculo de salud: 100 - (cantidad de sugerencias * 10), mínimo 0
        double health = Math.max(0.0, 100.0 - (suggestions.size() * 10.0));

        List<String> suggestionMessages = suggestions.stream()
                .map(ParcelaSuggestion::getMessage)
                .collect(Collectors.toList());

        ParcelaDetailDto dto = new ParcelaDetailDto();
        dto.parcela = ParcelaAppMapper.toResponseDto(parcela);
        dto.saludPorcentaje = health;
        dto.healthPercentage = health; // retro-compatibilidad

        // Coordenadas de la ubicación
        Location location = parcela.getLocation();
        if (location != null && location.getCoordinates() != null) {
            dto.latitud = location.getCoordinates().getY();
            dto.longitud = location.getCoordinates().getX();
        }

        // Datos agronómicos
        SistemaRiego sistemaRiego = parcela.getSistemaRiego();
        dto.sistemaRiego = sistemaRiego != null ? sistemaRiego.getNombre() : null;
        dto.phSuelo = parcela.getPhSuelo();
        dto.fechaSiembra = parcela.getFechaSiembra() != null ? parcela.getFechaSiembra().toString() : null;
        dto.fechaCosecha = parcela.getFechaCosecha() != null ? parcela.getFechaCosecha().toString() : null;

        dto.sugerencias = suggestionMessages;
        dto.suggestions = suggestionMessages; // retro-compatibilidad

        // TODO: poblar desde tabla Historial_Parcela cuando sea creada.
        dto.historial = Collections.emptyList();

        return dto;
    }
}
