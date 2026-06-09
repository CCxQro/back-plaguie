package itesm.mx.application.usecase.alerta;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.mapper.alerta.AlertaDtoMapper;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Devuelve alertas validadas cuyo centro esté dentro del radio (haversine) de
 * las coordenadas GPS de la parcela indicada (HU-28 / SCRUM-326).
 * El filtrado se realiza in-memory para evitar dependencias de tipos espaciales
 * que no son soportados por H2 en tests (ver SCRUM-321).
 */
@ApplicationScoped
public class GetAlertasCercanasParcelaUseCase {

    static final double DEFAULT_RADIO_KM = 50.0;

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Inject
    AlertaRepository alertaRepository;

    /**
     * @param lat     latitud del centro de la parcela
     * @param lon     longitud del centro de la parcela
     * @param radioKm radio de búsqueda en kilómetros (por defecto {@link #DEFAULT_RADIO_KM})
     * @return lista de alertas validadas dentro del radio, mapeadas a {@link GetAlertaResponseDto}
     */
    public List<GetAlertaResponseDto> execute(double lat, double lon, double radioKm) {
        double radio = radioKm <= 0 ? DEFAULT_RADIO_KM : radioKm;

        // Solo alertas validadas (statusId = 1) de los últimos 3 meses
        LocalDateTime since = LocalDateTime.now().minusMonths(3);

        List<GetAlertaResponseDto> resultado = new ArrayList<>();
        for (Alerta alerta : alertaRepository.findValidatedSince(since)) {
            if (alerta.getLatitude() == null || alerta.getLongitude() == null) {
                continue;
            }
            double distancia = haversineKm(lat, lon, alerta.getLatitude(), alerta.getLongitude());
            if (distancia <= radio) {
                resultado.add(AlertaDtoMapper.toResponseDto(alerta));
            }
        }
        return resultado;
    }

    /** Distancia en kilómetros entre dos puntos lat/lng mediante la fórmula haversine. */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
