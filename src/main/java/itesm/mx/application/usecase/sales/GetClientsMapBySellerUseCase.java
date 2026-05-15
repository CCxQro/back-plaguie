package itesm.mx.application.usecase.sales;

import itesm.mx.application.dto.ClientMapDto;
import itesm.mx.application.mapper.sales.ClientDtoMapper;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class GetClientsMapBySellerUseCase {

    private static final List<String> SEVERITY_ORDER = List.of("baja", "media", "alta", "critico", "critica");

    @Inject TechnicalSellerRepository technicalSellerRepository;
    @Inject OrderRepository orderRepository;
    @Inject ParcelaRepository parcelaRepository;
    @Inject AlertaRepository alertaRepository;

    public static class Filters {
        public String cultivo;
        public String estadoParcela;
        public String state;
        public String municipality;
        public Boolean onlyWithActiveAlerts;
    }

    @Transactional
    public List<ClientMapDto> execute(Long userId, Filters filters) {
        if (userId == null) {
            throw new IllegalArgumentException("El id de usuario es requerido");
        }

        Long sellerId = technicalSellerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró vendedor para el usuario con id: " + userId))
                .getTechnicalSellerId();

        Map<Long, List<Order>> ordersByFarmer = new LinkedHashMap<>();
        for (Order order : orderRepository.findAllBySellerIdWithFarmerLocation(sellerId)) {
            if (order.getFarmer() == null || order.getFarmer().getFarmerId() == null) continue;
            ordersByFarmer.computeIfAbsent(order.getFarmer().getFarmerId(), k -> new ArrayList<>())
                    .add(order);
        }

        List<ClientMapDto> result = new ArrayList<>();
        Filters f = filters != null ? filters : new Filters();

        for (Map.Entry<Long, List<Order>> entry : ordersByFarmer.entrySet()) {
            Order anyOrder = entry.getValue().get(0);
            Farmer farmer = anyOrder.getFarmer();
            User user = farmer.getUser();
            Location location = farmer.getLocation();

            List<Parcela> parcelas = parcelaRepository.findByFarmerId(farmer.getFarmerId());
            List<Alerta> alertas = user != null
                    ? alertaRepository.findByReportedUserId(user.getUserId())
                    : List.of();

            if (!matchesFilters(f, parcelas, alertas, location)) continue;

            result.add(buildDto(farmer, user, location, parcelas, alertas, entry.getValue()));
        }

        return result;
    }

    private ClientMapDto buildDto(Farmer farmer, User user, Location location,
                                  List<Parcela> parcelas, List<Alerta> alertas, List<Order> orders) {
        Double latitude = null;
        Double longitude = null;
        Long locationId = null;
        String stateName = null;
        String municipalityName = null;
        String localityName = null;

        if (location != null) {
            locationId = location.getLocationId();
            Point coords = location.getCoordinates();
            if (coords != null) {
                latitude = coords.getY();
                longitude = coords.getX();
            }
            if (location.getState() != null) stateName = location.getState().getName();
            if (location.getMunicipality() != null) municipalityName = location.getMunicipality().getName();
            if (location.getLocality() != null) localityName = location.getLocality().getName();
        }

        List<String> cultivos = parcelas.stream()
                .filter(p -> p.getTipoCultivo() != null && p.getTipoCultivo().getNombre() != null)
                .map(p -> p.getTipoCultivo().getNombre())
                .distinct()
                .toList();

        List<String> estados = parcelas.stream()
                .filter(p -> p.getEstadoParcela() != null && p.getEstadoParcela().getNombre() != null)
                .map(p -> p.getEstadoParcela().getNombre())
                .distinct()
                .toList();

        double totalHectareas = parcelas.stream()
                .map(Parcela::getTamanoHectareas)
                .filter(h -> h != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        List<Alerta> activeAlertas = alertas.stream()
                .filter(ClientDtoMapper::isActive)
                .toList();

        String maxSeverity = activeAlertas.stream()
                .map(Alerta::getSeveridad)
                .filter(s -> s != null)
                .max(Comparator.comparingInt(GetClientsMapBySellerUseCase::severityRank))
                .orElse(null);

        LocalDateTime lastOrderDate = ClientDtoMapper.maxOrderDate(orders);

        return new ClientMapDto(
                farmer.getFarmerId(),
                user != null ? user.getUserId() : null,
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                latitude,
                longitude,
                locationId,
                stateName,
                municipalityName,
                localityName,
                cultivos,
                estados,
                parcelas.size(),
                totalHectareas,
                !activeAlertas.isEmpty(),
                activeAlertas.size(),
                maxSeverity,
                orders.size(),
                lastOrderDate
        );
    }

    private boolean matchesFilters(Filters f, List<Parcela> parcelas, List<Alerta> alertas, Location location) {
        if (f.cultivo != null && !f.cultivo.isBlank()) {
            boolean match = parcelas.stream().anyMatch(p ->
                    p.getTipoCultivo() != null
                            && p.getTipoCultivo().getNombre() != null
                            && p.getTipoCultivo().getNombre().equalsIgnoreCase(f.cultivo));
            if (!match) return false;
        }
        if (f.estadoParcela != null && !f.estadoParcela.isBlank()) {
            boolean match = parcelas.stream().anyMatch(p ->
                    p.getEstadoParcela() != null
                            && p.getEstadoParcela().getNombre() != null
                            && p.getEstadoParcela().getNombre().equalsIgnoreCase(f.estadoParcela));
            if (!match) return false;
        }
        if (f.state != null && !f.state.isBlank()) {
            String name = location != null && location.getState() != null
                    ? location.getState().getName() : null;
            if (name == null || !name.equalsIgnoreCase(f.state)) return false;
        }
        if (f.municipality != null && !f.municipality.isBlank()) {
            String name = location != null && location.getMunicipality() != null
                    ? location.getMunicipality().getName() : null;
            if (name == null || !name.equalsIgnoreCase(f.municipality)) return false;
        }
        if (Boolean.TRUE.equals(f.onlyWithActiveAlerts)) {
            boolean hasActive = alertas.stream().anyMatch(ClientDtoMapper::isActive);
            if (!hasActive) return false;
        }
        return true;
    }

    private static int severityRank(String severity) {
        if (severity == null) return -1;
        return SEVERITY_ORDER.indexOf(severity.toLowerCase(Locale.ROOT));
    }
}
