package itesm.mx.application.usecase.sales;

import itesm.mx.application.dto.ClientMapDto;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.TipoCultivo;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetClientsMapBySellerUseCaseTest {

    @Mock TechnicalSellerRepository technicalSellerRepository;
    @Mock OrderRepository orderRepository;
    @Mock ParcelaRepository parcelaRepository;
    @Mock AlertaRepository alertaRepository;

    @InjectMocks
    GetClientsMapBySellerUseCase useCase;

    private Point point(double lon, double lat) {
        return new GeometryFactory().createPoint(new Coordinate(lon, lat));
    }

    private Farmer buildFarmer(Long farmerId, Long userId, String name, String email,
                                Long locationId, String state, String municipality,
                                double lon, double lat) {
        State stateModel = new State();
        stateModel.setName(state);

        Municipality municipalityModel = new Municipality();
        municipalityModel.setName(municipality);

        Location location = new Location();
        location.setLocationId(locationId);
        location.setCoordinates(point(lon, lat));
        location.setState(stateModel);
        location.setMunicipality(municipalityModel);

        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setLocation(location);

        Farmer farmer = new Farmer();
        farmer.setFarmerId(farmerId);
        farmer.setUser(user);
        farmer.setActive(true);
        return farmer;
    }

    private Order order(Long orderId, Farmer farmer) {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        return new Order(orderId, farmer, seller, LocalDateTime.now(),
                new OrderStatus(1L, "Pendiente"), BigDecimal.valueOf(500), List.of());
    }

    private Parcela parcela(Long id, String nombre, String cultivoNombre, String estadoNombre, double hectareas) {
        TipoCultivo tipo = new TipoCultivo();
        tipo.setNombre(cultivoNombre);

        EstadoParcela estado = new EstadoParcela();
        estado.setNombre(estadoNombre);

        Parcela p = new Parcela();
        p.setParcelaId(id);
        p.setNombreParcela(nombre);
        p.setTamanoHectareas(hectareas);
        p.setTipoCultivo(tipo);
        p.setEstadoParcela(estado);
        return p;
    }

    private Alerta alerta(Long id, String severidad, LocalDateTime validatedAt) {
        Alerta a = new Alerta();
        a.setAlertaId(id);
        a.setSeveridad(severidad);
        a.setValidatedAt(validatedAt);
        return a;
    }

    private TechnicalSeller seller(Long id) {
        TechnicalSeller s = new TechnicalSeller();
        s.setTechnicalSellerId(id);
        return s;
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, null));
        verifyNoInteractions(orderRepository, parcelaRepository, alertaRepository);
    }

    @Test
    void execute_WhenSellerNotFound_ThrowsIllegalArgumentException() {
        when(technicalSellerRepository.findByIdUser(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(99L, null));
        verifyNoInteractions(orderRepository, parcelaRepository, alertaRepository);
    }

    @Test
    void execute_AggregatesOrdersParcelasAndAlertsByFarmer() {
        Farmer farmer = buildFarmer(10L, 100L, "Juan", "juan@test.mx",
                200L, "Querétaro", "El Marqués", -100.39, 20.59);
        Order o1 = order(1L, farmer);
        Order o2 = order(2L, farmer);

        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller(1L)));
        when(orderRepository.findAllBySellerIdWithFarmerLocation(1L)).thenReturn(List.of(o1, o2));
        when(parcelaRepository.findByFarmerId(10L)).thenReturn(List.of(
                parcela(1L, "P1", "Maíz", "Siembra", 2.5),
                parcela(2L, "P2", "Trigo", "Crecimiento", 1.0)
        ));
        when(alertaRepository.findByReportedUserId(100L)).thenReturn(List.of(
                alerta(1L, "alta", null),
                alerta(2L, "media", LocalDateTime.now())
        ));

        List<ClientMapDto> result = useCase.execute(1L, null);

        assertEquals(1, result.size());
        ClientMapDto dto = result.get(0);
        assertEquals(10L, dto.farmerId);
        assertEquals("Juan", dto.name);
        assertEquals("juan@test.mx", dto.email);
        assertEquals(20.59, dto.latitude);
        assertEquals(-100.39, dto.longitude);
        assertEquals("Querétaro", dto.state);
        assertEquals("El Marqués", dto.municipality);
        assertEquals(2, dto.parcelasCount);
        assertEquals(3.5, dto.totalHectareas);
        assertTrue(dto.cultivos.containsAll(List.of("Maíz", "Trigo")));
        assertTrue(dto.estadosParcela.containsAll(List.of("Siembra", "Crecimiento")));
        assertEquals(2, dto.totalOrders);
        assertTrue(dto.hasActiveAlerts);
        assertEquals(1, dto.activeAlertsCount);
        assertEquals("alta", dto.maxAlertSeverity);
    }

    @Test
    void execute_WhenCultivoFilterDoesNotMatch_ExcludesFarmer() {
        Farmer farmer = buildFarmer(10L, 100L, "Juan", "j@t.mx",
                200L, "Querétaro", "El Marqués", -100.39, 20.59);

        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller(1L)));
        when(orderRepository.findAllBySellerIdWithFarmerLocation(1L))
                .thenReturn(List.of(order(1L, farmer)));
        when(parcelaRepository.findByFarmerId(10L)).thenReturn(List.of(
                parcela(1L, "P1", "Maíz", "Siembra", 1.0)));
        when(alertaRepository.findByReportedUserId(100L)).thenReturn(List.of());

        GetClientsMapBySellerUseCase.Filters filters = new GetClientsMapBySellerUseCase.Filters();
        filters.cultivo = "Trigo";

        List<ClientMapDto> result = useCase.execute(1L, filters);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenOnlyWithActiveAlertsAndNoneAreActive_ExcludesFarmer() {
        Farmer farmer = buildFarmer(10L, 100L, "Juan", "j@t.mx",
                200L, "Querétaro", "El Marqués", -100.39, 20.59);

        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller(1L)));
        when(orderRepository.findAllBySellerIdWithFarmerLocation(1L))
                .thenReturn(List.of(order(1L, farmer)));
        when(parcelaRepository.findByFarmerId(10L)).thenReturn(List.of());
        when(alertaRepository.findByReportedUserId(100L))
                .thenReturn(List.of(alerta(1L, "alta", LocalDateTime.now())));

        GetClientsMapBySellerUseCase.Filters filters = new GetClientsMapBySellerUseCase.Filters();
        filters.onlyWithActiveAlerts = true;

        List<ClientMapDto> result = useCase.execute(1L, filters);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenStateFilterDoesNotMatch_ExcludesFarmer() {
        Farmer farmer = buildFarmer(10L, 100L, "Juan", "j@t.mx",
                200L, "Querétaro", "El Marqués", -100.39, 20.59);

        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller(1L)));
        when(orderRepository.findAllBySellerIdWithFarmerLocation(1L))
                .thenReturn(List.of(order(1L, farmer)));
        when(parcelaRepository.findByFarmerId(10L)).thenReturn(List.of());
        when(alertaRepository.findByReportedUserId(100L)).thenReturn(List.of());

        GetClientsMapBySellerUseCase.Filters filters = new GetClientsMapBySellerUseCase.Filters();
        filters.state = "Jalisco";

        List<ClientMapDto> result = useCase.execute(1L, filters);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenFarmerHasNoActiveAlerts_HasActiveAlertsFalse() {
        Farmer farmer = buildFarmer(10L, 100L, "Juan", "j@t.mx",
                200L, "Querétaro", "El Marqués", -100.39, 20.59);

        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller(1L)));
        when(orderRepository.findAllBySellerIdWithFarmerLocation(1L))
                .thenReturn(List.of(order(1L, farmer)));
        when(parcelaRepository.findByFarmerId(10L)).thenReturn(List.of());
        when(alertaRepository.findByReportedUserId(100L))
                .thenReturn(List.of(alerta(1L, "alta", LocalDateTime.now())));

        List<ClientMapDto> result = useCase.execute(1L, null);
        assertEquals(1, result.size());
        assertFalse(result.get(0).hasActiveAlerts);
        assertEquals(0, result.get(0).activeAlertsCount);
    }
}
