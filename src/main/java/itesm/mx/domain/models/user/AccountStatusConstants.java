package itesm.mx.domain.models.user;

/**
 * Status IDs from the shared {@code Status} catalog table, used to drive the
 * account approval workflow for self-registered farmers (HU-23, SRS §1.4.1).
 *
 * <pre>
 *   1 = Accepted   -> account approved, can log in
 *   2 = Revision   -> pending administrator approval
 *   3 = Rejected   -> account rejected, access denied
 *   4 = Inactivo   -> deactivated
 * </pre>
 */
public final class AccountStatusConstants {

    private AccountStatusConstants() {
        // Restrict instantiation
    }

    public static final Long ACCEPTED = 1L;
    public static final Long REVISION = 2L;
    public static final Long REJECTED = 3L;
    public static final Long INACTIVE = 4L;
}
