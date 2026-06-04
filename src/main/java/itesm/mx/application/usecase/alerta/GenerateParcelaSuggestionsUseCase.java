package itesm.mx.application.usecase.alerta;

import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.parcela.ParcelaSuggestionRepository;
import itesm.mx.application.usecase.region.GetNearbyEarlyAlertsUseCase; // Need haversine distance logic
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class GenerateParcelaSuggestionsUseCase {

    private static final double ZONE_RADIUS_KM = 50.0; // Assume 50km is the "zone"

    @Inject
    ParcelaRepository parcelaRepository;

    @Inject
    ParcelaSuggestionRepository parcelaSuggestionRepository;

    public void execute(Alerta alerta) {
        if (alerta == null || alerta.getLatitude() == null || alerta.getLongitude() == null) {
            return;
        }

        List<Parcela> allParcelas = parcelaRepository.findAllParcelas();
        for (Parcela p : allParcelas) {
            if (p.getLocation() != null && p.getLocation().getCoordinates() != null) {
                double pLat = p.getLocation().getCoordinates().getY();
                double pLng = p.getLocation().getCoordinates().getX();
                double distance = GetNearbyEarlyAlertsUseCase.haversineKm(
                        pLat, pLng,
                        alerta.getLatitude(),
                        alerta.getLongitude()
                );
                
                if (distance <= ZONE_RADIUS_KM) {
                    ParcelaSuggestion suggestion = new ParcelaSuggestion();
                    suggestion.setParcela(p);
                    suggestion.setAlerta(alerta);
                    suggestion.setCreatedAt(LocalDateTime.now());
                    suggestion.setMessage("Alerta de plaga '" + alerta.getTipoPlaga() + "' detectada a " 
                            + String.format("%.1f", distance) + " km de su parcela. " 
                            + "Considere revisión preventiva.");
                    
                    parcelaSuggestionRepository.save(suggestion);
                }
            }
        }
    }
}
