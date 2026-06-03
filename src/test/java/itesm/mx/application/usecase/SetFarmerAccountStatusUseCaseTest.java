package itesm.mx.application.usecase;

import itesm.mx.application.usecase.users.subUsers.SetFarmerAccountStatusUseCase;
import itesm.mx.domain.models.user.AccountStatusConstants;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.FarmerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetFarmerAccountStatusUseCaseTest {

    @Mock
    private FarmerRepository farmerRepository;

    @InjectMocks
    private SetFarmerAccountStatusUseCase useCase;

    private Farmer pendingFarmer(Long farmerId, Long userId) {
        User user = new User();
        user.setUserId(userId);
        return new Farmer(farmerId, user, true, AccountStatusConstants.REVISION);
    }

    @Test
    void execute_Approve_SetsAcceptedStatus() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(pendingFarmer(10L, 6L)));
        when(farmerRepository.update(any(Farmer.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(6L, AccountStatusConstants.ACCEPTED);

        ArgumentCaptor<Farmer> captor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).update(captor.capture());
        assertEquals(AccountStatusConstants.ACCEPTED, captor.getValue().getStatusId());
    }

    @Test
    void execute_Reject_SetsRejectedStatus() {
        when(farmerRepository.findByIdUser(7L)).thenReturn(Optional.of(pendingFarmer(11L, 7L)));
        when(farmerRepository.update(any(Farmer.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(7L, AccountStatusConstants.REJECTED);

        ArgumentCaptor<Farmer> captor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).update(captor.capture());
        assertEquals(AccountStatusConstants.REJECTED, captor.getValue().getStatusId());
    }

    @Test
    void execute_WhenFarmerNotFound_ThrowsIllegalState() {
        when(farmerRepository.findByIdUser(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,
                () -> useCase.execute(99L, AccountStatusConstants.ACCEPTED));
    }

    @Test
    void execute_WhenInvalidUserId_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(0L, AccountStatusConstants.ACCEPTED));
    }

    @Test
    void execute_WhenNullStatus_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(6L, null));
    }
}
