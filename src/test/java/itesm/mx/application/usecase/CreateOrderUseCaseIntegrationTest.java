package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.OrderDetailItemDto;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.RegisterOrderDto;
import itesm.mx.application.usecase.order.CreateOrderUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.location.LocalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.MunicipalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.PropertyEntity;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.CategoryEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.ColorEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProviderEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.UnitEntity;
import itesm.mx.infrastructure.persistence.entity.order.OrderStatusEntity;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.order.OrderDetailRepositoryImpl;
import itesm.mx.infrastructure.persistence.repository.order.OrderRepositoryImpl;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(UseCaseIntegrationTestProfile.class)
class CreateOrderUseCaseIntegrationTest {

    @Inject CreateOrderUseCase createOrderUseCase;
    @Inject OrderRepositoryImpl orderRepository;
    @Inject OrderDetailRepositoryImpl orderDetailRepository;
    @Inject EntityManager em;

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;

    private Long farmerId;
    private Long sellerId;
    private Long orderStatusId;
    private Long productId;

    @BeforeEach
    @Transactional
    void setup() {
        em.createQuery("delete from OrderDetailEntity").executeUpdate();
        em.createQuery("delete from OrderEntity").executeUpdate();
        em.createQuery("delete from OrderStatusEntity").executeUpdate();
        em.createQuery("delete from ProductEntity").executeUpdate();
        em.createQuery("delete from CategoryEntity").executeUpdate();
        em.createQuery("delete from ColorEntity").executeUpdate();
        em.createQuery("delete from ProviderEntity").executeUpdate();
        em.createQuery("delete from UnitEntity").executeUpdate();
        em.createQuery("delete from StatusEntity").executeUpdate();
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
        location.coordinates = new GeometryFactory().createPoint(new Coordinate(-103.48, 20.75));
        location.stateId = state.stateId;
        location.municipalityId = municipality.municipalityId;
        location.localityId = locality.localityId;
        location.propertyId = property.propertyId;
        em.persist(location);

        UserEntity farmerUser = new UserEntity();
        farmerUser.firebaseUuid = "uuid-create-farmer";
        farmerUser.name = "Farmer Create";
        farmerUser.email = "create.farmer@itesm.mx";
        farmerUser.roleId = 2;
        farmerUser.isActive = true;
        farmerUser.locationId = location.locationId;
        em.persist(farmerUser);

        FarmerEntity farmer = new FarmerEntity();
        farmer.userId = farmerUser.userId;
        farmer.isActive = true;
        em.persist(farmer);
        farmerId = farmer.farmerId;

        UserEntity sellerUser = new UserEntity();
        sellerUser.firebaseUuid = "uuid-create-seller";
        sellerUser.name = "Seller Create";
        sellerUser.email = "create.seller@itesm.mx";
        sellerUser.roleId = 3;
        sellerUser.isActive = true;
        sellerUser.locationId = location.locationId;
        em.persist(sellerUser);

        TechnicalSellerEntity seller = new TechnicalSellerEntity();
        seller.userId = sellerUser.userId;
        seller.isActive = true;
        em.persist(seller);
        sellerId = seller.technicalSellerId;

        OrderStatusEntity status = new OrderStatusEntity();
        status.estado = "Pendiente";
        em.persist(status);
        orderStatusId = status.orderStatusId;

        ColorEntity color = new ColorEntity();
        color.name = "Rojo";
        color.hexa = "#FF0000";
        em.persist(color);

        StatusEntity mktStatus = new StatusEntity();
        mktStatus.name = "Accepted";
        em.persist(mktStatus);

        UserEntity adminUser = new UserEntity();
        adminUser.firebaseUuid = "uuid-create-admin";
        adminUser.name = "Admin Create";
        adminUser.email = "create.admin@itesm.mx";
        adminUser.roleId = 1;
        adminUser.isActive = true;
        em.persist(adminUser);

        CategoryEntity category = new CategoryEntity();
        category.userId = adminUser.userId;
        category.name = "Fertilizantes Test";
        category.colorId = color.colorId;
        category.statusId = mktStatus.statusId;
        em.persist(category);

        ProviderEntity provider = new ProviderEntity();
        provider.userId = sellerUser.userId;
        provider.name = "Proveedor Test";
        em.persist(provider);

        UnitEntity unit = new UnitEntity();
        unit.userId = adminUser.userId;
        unit.name = "Kilogramo Test";
        unit.statusId = mktStatus.statusId;
        em.persist(unit);

        ProductEntity product = new ProductEntity();
        product.skuSellerId = 9001L;
        product.sellerId = sellerId;
        product.name = "Producto Test";
        product.sku = "TEST-001";
        product.categoryId = category.categoryId;
        product.providerId = provider.providerId;
        product.unitValue = 200.0;
        product.unitId = unit.unitId;
        product.description = "Descripcion test";
        product.statusId = mktStatus.statusId;
        em.persist(product);
        productId = product.skuSellerId;
    }

    @Test
    void execute_HappyPath_OrderAndDetailsPersistToDatabase() {
        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = farmerId;
        dto.sellerId = sellerId;
        dto.orderStatusId = orderStatusId;
        dto.totalAmount = BigDecimal.valueOf(400);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = productId;
        item.quantity = 2;
        item.unitPrice = 200.0f;
        dto.details = List.of(item);

        OrderResponseDto result = createOrderUseCase.execute(dto);

        assertNotNull(result);
        assertNotNull(result.orderId);
        assertEquals(farmerId, result.farmerId);
        assertEquals(sellerId, result.sellerId);

        assertEquals(1, orderDetailRepository.findAllByOrderId(result.orderId).size());
    }

    @Test
    void execute_PersistsCorrectDetailFields() {
        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = farmerId;
        dto.sellerId = sellerId;
        dto.orderStatusId = orderStatusId;
        dto.totalAmount = BigDecimal.valueOf(400);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = productId;
        item.quantity = 3;
        item.unitPrice = 150.0f;
        dto.details = List.of(item);

        OrderResponseDto result = createOrderUseCase.execute(dto);

        var details = orderDetailRepository.findAllByOrderId(result.orderId);
        assertEquals(1, details.size());
        assertEquals(3, details.get(0).getQuantity());
        assertEquals(150.0f, details.get(0).getUnitPrice());
    }

    @Test
    void execute_WhenFarmerNotFound_ThrowsIllegalArgumentException() {
        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = 99999L;
        dto.sellerId = sellerId;
        dto.orderStatusId = orderStatusId;
        dto.totalAmount = BigDecimal.valueOf(400);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = productId;
        item.quantity = 1;
        item.unitPrice = 400.0f;
        dto.details = List.of(item);

        assertThrows(IllegalArgumentException.class, () -> createOrderUseCase.execute(dto));
        assertEquals(0, orderRepository.findAllBySellerId(sellerId).size());
    }

    @Test
    void execute_WhenProductNotFound_ThrowsIllegalArgumentException() {
        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = farmerId;
        dto.sellerId = sellerId;
        dto.orderStatusId = orderStatusId;
        dto.totalAmount = BigDecimal.valueOf(400);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = 99999L;
        item.quantity = 1;
        item.unitPrice = 400.0f;
        dto.details = List.of(item);

        assertThrows(IllegalArgumentException.class, () -> createOrderUseCase.execute(dto));
    }
}
