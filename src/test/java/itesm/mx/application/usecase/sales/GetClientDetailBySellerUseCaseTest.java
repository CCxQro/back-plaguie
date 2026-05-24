package itesm.mx.application.usecase.sales;

import itesm.mx.application.dto.ClientDetailDto;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.location.Location;
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
import itesm.mx.domain.repository.user.FarmerRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetClientDetailBySellerUseCaseTest {

    @Mock TechnicalSellerRepository technicalSellerRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock OrderRepository orderRepository;
    @Mock ParcelaRepository parcelaRepository;
    @Mock AlertaRepository alertaRepository;

    @InjectMocks
    GetClientDetailBySellerUseCase useCase;

    private TechnicalSeller seller(Long id) {
        TechnicalSeller s = new TechnicalSeller();
        s.setTechnicalSellerId(id);
        return s;
    }

    private Farmer farmer(Long farmerId, Long userId, String name, String email) {
        Location location = new Location();
        location.setLocationId(200L);
        location.setCoordinates(new GeometryFactory().createPoint(new Coordinate(-100.39, 20.59)));

        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setLocation(location);

        Farmer f = new Farmer();
        f.setFarmerId(farmerId);
        f.setUser(user);
        f.setActive(true);
        return f;
    }

    private Order order(Long id, Long farmerId, BigDecimal total, LocalDateTime date) {
        Farmer f = new Farmer();
        f.setFarmerId(farmerId);
        return new Order(id, f, seller(1L), date,
                new OrderStatus(1L, "Pendiente"), total, List.of());
    }

    private Parcela parcela(Long id, String nombre) {
        TipoCultivo tipo = new TipoCultivo();
        tipo.setNombre("Maíz");
        EstadoParcela estado = new EstadoParcela();
        estado.setNombre("Siembra");

        Parcela p = new Parcela();
        p.setParcelaId(id);
        p.setNombreParcela(nombre);
        p.setTamanoHectareas(2.0);
        p.setTipoCultivo(tipo);
        p.setEstadoParcela(estado);
        return p;
    }

    @Test
    void execute_WhenUserIdIsNull_Throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, 1L));
    }

    @Test
    void execute_WhenFarmerIdIsNull_Throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L, null));
    }

    @Test
    void execute_WhenSellerNotFound_Throws() {
        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L, 10L));
    }

    @Test
    void execute_WhenFarmerIsNotClientOfSeller_ThrowsIllegalState() {
        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller(1L)));
        when(orderRepository.findAllBySellerIdAndFarmerId(1L, 10L)).thenReturn(List.of());
        assertThrows(IllegalStateException.class, () -> useCase.execute(1L, 10L));
    }

    @Test
    void execute_ReturnsAggregatedDetail() {
        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller(1L)));
        LocalDateTime now = LocalDateTime.now();
        when(orderRepository.findAllBySellerIdAndFarmerId(1L, 10L)).thenReturn(List.of(
                order(1L, 10L, new BigDecimal("500.00"), now.minusDays(2)),
                order(2L, 10L, new BigDecimal("300.00"), now)
        ));
        when(farmerRepository.findByFarmerId(10L)).thenReturn(Optional.of(
                farmer(10L, 100L, "Juan", "juan@test.mx")));
        when(parcelaRepository.findByFarmerId(10L)).thenReturn(List.of(parcela(1L, "P1")));
        Alerta alerta = new Alerta();
        alerta.setAlertaId(1L);
        alerta.setSeveridad("alta");
        when(alertaRepository.findByReportedUserId(100L)).thenReturn(List.of(alerta));

        ClientDetailDto detail = useCase.execute(1L, 10L);

        assertEquals(10L, detail.farmerId);
        assertEquals(100L, detail.userId);
        assertEquals("Juan", detail.name);
        assertEquals("juan@test.mx", detail.email);
        assertEquals(20.59, detail.latitude);
        assertEquals(-100.39, detail.longitude);
        assertEquals(1, detail.parcelas.size());
        assertEquals(1, detail.alertas.size());
        assertNotNull(detail.orderSummary);
        assertEquals(2, detail.orderSummary.totalOrders);
        assertEquals(new BigDecimal("800.00"), detail.orderSummary.totalAmount);
        assertEquals(now, detail.orderSummary.lastOrderDate);
        assertEquals("Pendiente", detail.orderSummary.lastOrderStatus);
    }
}
