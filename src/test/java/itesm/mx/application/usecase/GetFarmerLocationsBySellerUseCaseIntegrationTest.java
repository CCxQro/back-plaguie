package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.FarmerLocationDto;
import itesm.mx.application.usecase.order.GetFarmerLocationsBySellerUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.location.LocalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.MunicipalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.PropertyEntity;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;
import itesm.mx.infrastructure.persistence.entity.order.OrderEntity;
import itesm.mx.infrastructure.persistence.entity.order.OrderStatusEntity;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(UseCaseIntegrationTestProfile.class)
class GetFarmerLocationsBySellerUseCaseIntegrationTest {

    @Inject GetFarmerLocationsBySellerUseCase getFarmerLocationsBySellerUseCase;
    @Inject EntityManager em;

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;

    private Long sellerUserId;
    private Long emptySellerUserId;
    private Long farmerId;
    private Long seedLocationId;
    private double expectedLat = 20.75;
    private double expectedLon = -103.48;

    @BeforeEach
    @Transactional
    void setup() {
        em.createQuery("delete from OrderDetailEntity").executeUpdate();
        em.createQuery("delete from OrderEntity").executeUpdate();
        em.createQuery("delete from OrderStatusEntity").executeUpdate();
        em.createQuery("delete from ProductEntity").executeUpdate();
        em.createQuery("delete from CategoryEntity").executeUpdate();
        em.createQuery("delete from ProviderEntity").executeUpdate();
        em.createQuery("delete from UnitEntity").executeUpdate();
        em.createQuery("delete from StatusEntity").executeUpdate();
        em.createQuery("delete from ColorEntity").executeUpdate();
        em.createQuery("delete from FarmerEntity").executeUpdate();
        em.createQuery("delete from TechnicalSellerEntity").executeUpdate();
        em.createQuery("delete from UserEntity").executeUpdate();
        em.createQuery("delete from LocationEntity").executeUpdate();
        em.createQuery("delete from LocalityEntity").executeUpdate();
        em.createQuery("delete from MunicipalityEntity").executeUpdate();
        em.createQuery("delete from PropertyEntity").executeUpdate();
        em.createQuery("delete from StateEntity").executeUpdate();

        StateEntity state = new StateEntity();
        state.name = "jalisco";
        em.persist(state);

        MunicipalityEntity municipality = new MunicipalityEntity();
        municipality.name = "zapopan";
        em.persist(municipality);

        LocalityEntity locality = new LocalityEntity();
        locality.name = "tesistan";
        em.persist(locality);

        PropertyEntity property = new PropertyEntity();
        property.name = "el milagro";
        em.persist(property);

        LocationEntity location = new LocationEntity();
        location.coordinates = new GeometryFactory()
                .createPoint(new Coordinate(expectedLon, expectedLat));
        location.stateId = state.stateId;
        location.municipalityId = municipality.municipalityId;
        location.localityId = locality.localityId;
        location.propertyId = property.propertyId;
        em.persist(location);
        seedLocationId = location.locationId;

        UserEntity farmerUser = new UserEntity();
        farmerUser.firebaseUuid = "uuid-loc-farmer";
        farmerUser.name = "Farmer Location";
        farmerUser.email = "loc.farmer@itesm.mx";
        farmerUser.roleId = 2;
        farmerUser.isActive = true;
        em.persist(farmerUser);

        FarmerEntity farmer = new FarmerEntity();
        farmer.userId = farmerUser.userId;
        farmer.locationId = location.locationId;
        farmer.isActive = true;
        em.persist(farmer);
        farmerId = farmer.farmerId;

        UserEntity sellerUser = new UserEntity();
        sellerUser.firebaseUuid = "uuid-loc-seller";
        sellerUser.name = "Seller Location";
        sellerUser.email = "loc.seller@itesm.mx";
        sellerUser.roleId = 3;
        sellerUser.isActive = true;
        em.persist(sellerUser);
        sellerUserId = sellerUser.userId;

        TechnicalSellerEntity seller = new TechnicalSellerEntity();
        seller.userId = sellerUser.userId;
        seller.locationId = location.locationId;
        seller.isActive = true;
        em.persist(seller);

        OrderStatusEntity status = new OrderStatusEntity();
        status.estado = "Pendiente";
        em.persist(status);

        OrderEntity order = new OrderEntity();
        order.farmerId = farmer.farmerId;
        order.sellerId = seller.technicalSellerId;
        order.orderDate = LocalDateTime.now();
        order.orderStatusId = status.orderStatusId;
        order.totalAmount = BigDecimal.valueOf(500);
        em.persist(order);

        // Pre-seed a seller with no orders for the empty-list test
        UserEntity emptySellerUser = new UserEntity();
        emptySellerUser.firebaseUuid = "uuid-loc-emptyseller";
        emptySellerUser.name = "Empty Seller";
        emptySellerUser.email = "loc.emptyseller@itesm.mx";
        emptySellerUser.roleId = 3;
        emptySellerUser.isActive = true;
        em.persist(emptySellerUser);
        emptySellerUserId = emptySellerUser.userId;

        TechnicalSellerEntity emptySeller = new TechnicalSellerEntity();
        emptySeller.userId = emptySellerUser.userId;
        emptySeller.locationId = seedLocationId;
        emptySeller.isActive = true;
        em.persist(emptySeller);
    }

    @Test
    void execute_ReturnsCoordinatesForSellersClients() {
        List<FarmerLocationDto> result = getFarmerLocationsBySellerUseCase.execute(sellerUserId);

        assertEquals(1, result.size());
        FarmerLocationDto dto = result.get(0);
        assertEquals(farmerId, dto.farmerId);
        assertNotNull(dto.latitude);
        assertNotNull(dto.longitude);
        assertEquals(expectedLat, dto.latitude, 0.001);
        assertEquals(expectedLon, dto.longitude, 0.001);
        assertNotNull(dto.locationId);
    }

    @Test
    void execute_FarmerNameResolvedFromLinkedUser() {
        List<FarmerLocationDto> result = getFarmerLocationsBySellerUseCase.execute(sellerUserId);

        assertEquals("Farmer Location", result.get(0).farmerName);
    }

    @Test
    void execute_WhenSellerHasNoOrders_ReturnsEmptyList() {
        List<FarmerLocationDto> result =
                getFarmerLocationsBySellerUseCase.execute(emptySellerUserId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenSellerNotFound_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> getFarmerLocationsBySellerUseCase.execute(99999L));
    }
}
