package itesm.mx.application.usecase;

import itesm.mx.application.dto.FarmerLocationDto;
import itesm.mx.application.usecase.order.GetFarmerLocationsBySellerUseCase;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.order.OrderRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFarmerLocationsBySellerUseCaseTest {

    @Mock OrderRepository orderRepository;
    @Mock TechnicalSellerRepository technicalSellerRepository;

    @InjectMocks
    GetFarmerLocationsBySellerUseCase getFarmerLocationsBySellerUseCase;

    private Point buildPoint(double lon, double lat) {
        return new GeometryFactory().createPoint(new Coordinate(lon, lat));
    }

    private Order buildOrderWithLocation(Long orderId, Long farmerId, String farmerName,
                                          double lon, double lat, Long locationId) {
        Location location = new Location();
        location.setLocationId(locationId);
        location.setCoordinates(buildPoint(lon, lat));

        User user = new User();
        user.setName(farmerName);
        user.setLocation(location);

        Farmer farmer = new Farmer();
        farmer.setFarmerId(farmerId);
        farmer.setUser(user);

        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);

        return new Order(orderId, farmer, seller, LocalDateTime.now(),
                new OrderStatus(1L, "Pendiente"), BigDecimal.valueOf(500), List.of());
    }

    @Test
    void execute_ResolvesTechnicalSellerIdFromUserId() {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(2L);
        when(technicalSellerRepository.findByIdUser(11L)).thenReturn(Optional.of(seller));
        when(orderRepository.findAllBySellerIdWithFarmerLocation(2L)).thenReturn(List.of());

        getFarmerLocationsBySellerUseCase.execute(11L);

        verify(orderRepository).findAllBySellerIdWithFarmerLocation(2L);
    }

    @Test
    void execute_MapsCoordinatesCorrectly() {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        when(technicalSellerRepository.findByIdUser(11L)).thenReturn(Optional.of(seller));
        Order order = buildOrderWithLocation(1L, 1L, "Juan", -103.48, 20.75, 1L);
        when(orderRepository.findAllBySellerIdWithFarmerLocation(1L)).thenReturn(List.of(order));

        List<FarmerLocationDto> result = getFarmerLocationsBySellerUseCase.execute(11L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).farmerId);
        assertEquals("Juan", result.get(0).farmerName);
        assertEquals(20.75, result.get(0).latitude);
        assertEquals(-103.48, result.get(0).longitude);
        assertEquals(1L, result.get(0).locationId);
    }

    @Test
    void execute_WhenSellerNotFound_ThrowsIllegalArgumentException() {
        when(technicalSellerRepository.findByIdUser(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> getFarmerLocationsBySellerUseCase.execute(99L));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> getFarmerLocationsBySellerUseCase.execute(null));
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(technicalSellerRepository);
    }
}
