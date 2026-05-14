package itesm.mx.application.usecase.vigilancia;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ValidateUseCaseTest {

    @Inject
    ValidateVigilanciaFitosanitariaUseCase useCase;

    @Test
    public void testValidate() {
        try {
            useCase.execute(3L, 1L, 1L);
            System.out.println("SUCCESS!");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
