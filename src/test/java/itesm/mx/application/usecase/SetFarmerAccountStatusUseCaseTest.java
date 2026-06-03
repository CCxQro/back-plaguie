package itesm.mx.application.usecase;

import itesm.mx.application.usecase.users.subUsers.SetFarmerAccountStatusUseCase;
import itesm.mx.domain.models.user.AccountStatusConstants;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.UserRepository;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SetFarmerAccountStatusUseCase useCase;

    private Farmer pendingFarmer(Long farmerId, Long userId) {
        User user = new User();
        user.setUserId(userId);
        return new Farmer(farmerId, user, true, AccountStatusConstants.REVISION);
    }

    private User userWithId(Long userId) {
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    @Test
    void execute_Approve_SetsAcceptedStatus() {
        when(farmerRepository.findByIdUser(6L)).thenReturn(Optional.of(pendingFarmer(10L, 6L)));
        when(farmerRepository.update(any(Farmer.class))).thenAnswer(inv -> inv.getArgument(0));
        // Approve/reject also sync UserEntity.isActive, so the user must be resolvable.
        when(userRepository.findUserById(6L)).thenReturn(Optional.of(userWithId(6L)));

        useCase.execute(6L, AccountStatusConstants.ACCEPTED);

        ArgumentCaptor<Farmer> captor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).update(captor.capture());
        assertEquals(AccountStatusConstants.ACCEPTED, captor.getValue().getStatusId());

        // The base user must be (re)activated on approval.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(userCaptor.capture());
        assertEquals(Boolean.TRUE, userCaptor.getValue().getActive());
    }

    @Test
    void execute_Reject_SetsRejectedStatus() {
        when(farmerRepository.findByIdUser(7L)).thenReturn(Optional.of(pendingFarmer(11L, 7L)));
        when(farmerRepository.update(any(Farmer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findUserById(7L)).thenReturn(Optional.of(userWithId(7L)));

        useCase.execute(7L, AccountStatusConstants.REJECTED);

        ArgumentCaptor<Farmer> captor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).update(captor.capture());
        assertEquals(AccountStatusConstants.REJECTED, captor.getValue().getStatusId());

        // The base user must be deactivated on rejection.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(userCaptor.capture());
        assertEquals(Boolean.FALSE, userCaptor.getValue().getActive());
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
