package itesm.mx.application.usecase.marketplace.product;

import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.marketplace.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateProductUseCaseTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    StatusRepository statusRepository;

    @InjectMocks
    ValidateProductUseCase validateProductUseCase;

    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setSkuSellerId(1L);
        mockProduct.setName("Test Product");
        Status currentStatus = new Status();
        currentStatus.setStatusId(2L); // 2: Revision
        mockProduct.setStatus(currentStatus);
    }

    @Test
    void testExecuteSuccess() {
        Long skuSellerId = 1L;
        Long newStatusId = 1L; // 1: Accepted

        when(productRepository.findByProductId(skuSellerId)).thenReturn(Optional.of(mockProduct));
        
        Status newStatus = new Status();
        newStatus.setStatusId(newStatusId);
        when(statusRepository.findByStatusId(newStatusId)).thenReturn(Optional.of(newStatus));

        Product updatedProduct = new Product();
        updatedProduct.setSkuSellerId(skuSellerId);
        updatedProduct.setStatus(newStatus);
        when(productRepository.update(eq(skuSellerId), any(Product.class))).thenReturn(updatedProduct);

        Product result = validateProductUseCase.execute(skuSellerId, newStatusId);

        assertNotNull(result);
        assertEquals(newStatusId, result.getStatus().getStatusId());
        verify(productRepository).update(eq(skuSellerId), argThat(product -> product.getStatus().getStatusId().equals(newStatusId)));
    }

    @Test
    void testExecuteInvalidStatusIdThrowsException() {
        Long skuSellerId = 1L;
        Long invalidStatusId = 99L; // Only 1, 2, 3 are valid

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validateProductUseCase.execute(skuSellerId, invalidStatusId));
        assertEquals("El statusId debe ser 1 (Accepted), 2 (Revision) o 3 (Rejected)", exception.getMessage());
    }

    @Test
    void testExecuteProductNotFoundThrowsException() {
        Long skuSellerId = 1L;
        Long newStatusId = 1L;

        when(productRepository.findByProductId(skuSellerId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                validateProductUseCase.execute(skuSellerId, newStatusId));
        assertEquals("Producto no encontrado", exception.getMessage());
    }

    @Test
    void testExecuteStatusNotFoundThrowsException() {
        Long skuSellerId = 1L;
        Long newStatusId = 1L;

        when(productRepository.findByProductId(skuSellerId)).thenReturn(Optional.of(mockProduct));
        when(statusRepository.findByStatusId(newStatusId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validateProductUseCase.execute(skuSellerId, newStatusId));
        assertEquals("Status no encontrado", exception.getMessage());
    }
}
