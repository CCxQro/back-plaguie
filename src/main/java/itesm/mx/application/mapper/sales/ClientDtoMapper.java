package itesm.mx.application.mapper.sales;

import itesm.mx.application.dto.ClientAlertaSummaryDto;
import itesm.mx.application.dto.ClientDetailDto;
import itesm.mx.application.dto.ClientOrderSummaryDto;
import itesm.mx.application.dto.ClientParcelaSummaryDto;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ClientDtoMapper {

    private ClientDtoMapper() {}

    public static ClientParcelaSummaryDto toParcelaSummary(Parcela parcela) {
        return new ClientParcelaSummaryDto(
                parcela.getParcelaId(),
                parcela.getNombreParcela(),
                parcela.getTamanoHectareas(),
                parcela.getTipoCultivo() != null ? parcela.getTipoCultivo().getNombre() : null,
                parcela.getEstadoParcela() != null ? parcela.getEstadoParcela().getNombre() : null,
                parcela.getSistemaRiego() != null ? parcela.getSistemaRiego().getNombre() : null,
                parcela.getPhSuelo(),
                parcela.getFechaSiembra(),
                parcela.getFechaCosecha()
        );
    }

    public static ClientAlertaSummaryDto toAlertaSummary(Alerta alerta) {
        return new ClientAlertaSummaryDto(
                alerta.getAlertaId(),
                alerta.getTitulo(),
                alerta.getTipoPlaga(),
                alerta.getSeveridad(),
                alerta.getHectareas(),
                alerta.getCreatedAt(),
                alerta.getStatusId(),
                alerta.getStatusName(),
                isActive(alerta)
        );
    }

    public static ClientOrderSummaryDto toOrderSummary(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return new ClientOrderSummaryDto(0, BigDecimal.ZERO, null, null);
        }
        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order lastOrder = orders.stream()
                .filter(o -> o.getOrderDate() != null)
                .max(Comparator.comparing(Order::getOrderDate))
                .orElse(orders.get(0));

        String lastStatus = lastOrder.getOrderStatus() != null ? lastOrder.getOrderStatus().getEstado() : null;
        return new ClientOrderSummaryDto(orders.size(), totalAmount, lastOrder.getOrderDate(), lastStatus);
    }

    public static ClientDetailDto toDetailDto(Farmer farmer,
                                              List<Parcela> parcelas,
                                              List<Alerta> alertas,
                                              List<Order> orders) {
        User user = farmer.getUser();
        Location location = user != null ? user.getLocation() : null;

        Double latitude = null;
        Double longitude = null;
        Long locationId = null;
        String state = null;
        String municipality = null;
        String locality = null;
        String property = null;

        if (location != null) {
            locationId = location.getLocationId();
            Point coords = location.getCoordinates();
            if (coords != null) {
                latitude = coords.getY();
                longitude = coords.getX();
            }
            if (location.getState() != null) state = location.getState().getName();
            if (location.getMunicipality() != null) municipality = location.getMunicipality().getName();
            if (location.getLocality() != null) locality = location.getLocality().getName();
            if (location.getProperty() != null) property = location.getProperty().getName();
        }

        List<ClientParcelaSummaryDto> parcelaDtos = parcelas == null ? List.of()
                : parcelas.stream().map(ClientDtoMapper::toParcelaSummary).toList();

        List<ClientAlertaSummaryDto> alertaDtos = alertas == null ? List.of()
                : alertas.stream().map(ClientDtoMapper::toAlertaSummary).toList();

        ClientOrderSummaryDto orderSummary = toOrderSummary(orders);

        return new ClientDetailDto(
                farmer.getFarmerId(),
                user != null ? user.getUserId() : null,
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                farmer.getActive(),
                locationId,
                latitude,
                longitude,
                state,
                municipality,
                locality,
                property,
                parcelaDtos,
                alertaDtos,
                orderSummary
        );
    }

    public static boolean isActive(Alerta alerta) {
        if (alerta == null) return false;
        return alerta.getValidatedAt() == null;
    }

    public static LocalDateTime maxOrderDate(List<Order> orders) {
        if (orders == null) return null;
        return orders.stream()
                .map(Order::getOrderDate)
                .filter(d -> d != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
