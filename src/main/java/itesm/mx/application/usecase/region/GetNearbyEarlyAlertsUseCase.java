package itesm.mx.application.usecase.region;

import itesm.mx.application.dto.GetEarlyAlertaResponseDto;
import itesm.mx.application.mapper.alerta.EarlyAlertaDtoMapper;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.location.LocationRepository;
import itesm.mx.domain.repository.user.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Returns the validated pest alerts located within {@code radiusKm} of the
 * requesting user's location, created within the last
 * {@link #EARLY_ALERT_WINDOW_MONTHS} months, sorted by ascending distance
 * (HU-26 CA-02). Distance is computed server-side (haversine) from the user's
 * configured location, so the client doesn't need the user's coordinates.
 */
@ApplicationScoped
public class GetNearbyEarlyAlertsUseCase {

    /** Early alerts only consider validated alerts from the last N months. */
    static final int EARLY_ALERT_WINDOW_MONTHS = 3;

    static final double DEFAULT_RADIUS_KM = 100.0;

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Inject
    UserRepository userRepository;

    @Inject
    LocationRepository locationRepository;

    @Inject
    AlertaRepository alertaRepository;

    public List<GetEarlyAlertaResponseDto> execute(Long userId, Double radiusKm) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }
        double radius = (radiusKm == null || radiusKm <= 0) ? DEFAULT_RADIUS_KM : radiusKm;

        // Resolve the requesting user's coordinates.
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado con id: " + userId));
        Long locationId = user.getLocation() != null ? user.getLocation().getLocationId() : null;
        if (locationId == null) {
            throw new IllegalArgumentException(
                    "No tienes una ubicación configurada para calcular alertas cercanas");
        }
        Location location = locationRepository.findLocationById(locationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se pudo resolver tu ubicación para calcular alertas cercanas"));
        Point center = location.getCoordinates();
        if (center == null) {
            throw new IllegalArgumentException(
                    "Tu ubicación no tiene coordenadas para calcular alertas cercanas");
        }
        double originLat = center.getY();
        double originLng = center.getX();

        LocalDateTime since = LocalDateTime.now().minusMonths(EARLY_ALERT_WINDOW_MONTHS);

        List<Alerta> withinRadius = new ArrayList<>();
        for (Alerta alerta : alertaRepository.findValidatedSince(since)) {
            if (alerta.getLatitude() == null || alerta.getLongitude() == null) {
                continue;
            }
            double distance = haversineKm(originLat, originLng,
                    alerta.getLatitude(), alerta.getLongitude());
            if (distance <= radius) {
                alerta.setDistanceKm(Math.round(distance * 10.0) / 10.0);
                withinRadius.add(alerta);
            }
        }

        withinRadius.sort(Comparator.comparingDouble(
                a -> a.getDistanceKm() != null ? a.getDistanceKm() : Double.MAX_VALUE));

        return withinRadius.stream()
                .map(EarlyAlertaDtoMapper::toResponseDto)
                .toList();
    }

    /**
     * HU-28 (SCRUM-309): Returns early alerts near a specific parcela's GPS coordinates
     * instead of the user's profile location. Intended for authenticated farmers/sellers
     * who know the parcela's lat/lon.
     *
     * @param lat      parcela latitude
     * @param lon      parcela longitude
     * @param radiusKm search radius (defaults to {@link #DEFAULT_RADIUS_KM} when null/≤0)
     */
    public List<GetEarlyAlertaResponseDto> executeForCoordinates(double lat, double lon, Double radiusKm) {
        double radius = (radiusKm == null || radiusKm <= 0) ? DEFAULT_RADIUS_KM : radiusKm;
        LocalDateTime since = LocalDateTime.now().minusMonths(EARLY_ALERT_WINDOW_MONTHS);

        List<Alerta> withinRadius = new ArrayList<>();
        for (Alerta alerta : alertaRepository.findValidatedSince(since)) {
            if (alerta.getLatitude() == null || alerta.getLongitude() == null) {
                continue;
            }
            double distance = haversineKm(lat, lon, alerta.getLatitude(), alerta.getLongitude());
            if (distance <= radius) {
                alerta.setDistanceKm(Math.round(distance * 10.0) / 10.0);
                withinRadius.add(alerta);
            }
        }

        withinRadius.sort(Comparator.comparingDouble(
                a -> a.getDistanceKm() != null ? a.getDistanceKm() : Double.MAX_VALUE));

        return withinRadius.stream()
                .map(EarlyAlertaDtoMapper::toResponseDto)
                .toList();
    }

    /** Great-circle distance between two lat/lng points, in kilometers. */
    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
