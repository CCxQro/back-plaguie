package itesm.mx.application.ingestion;

import itesm.mx.infrastructure.ingestion.SsrfGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SsrfGuardTest {

    @Test
    void allowsDataGobMx() {
        assertDoesNotThrow(() -> SsrfGuard.check("https://datos.gob.mx/dataset/foo"));
    }

    @Test
    void allowsWwwDataGobMx() {
        assertDoesNotThrow(() -> SsrfGuard.check("https://www.datos.gob.mx/api/3/action/package_show?id=foo"));
    }

    @Test
    void allowsRepodatos() {
        assertDoesNotThrow(() -> SsrfGuard.check(
                "https://repodatos.atdt.gob.mx/api_update/senasica/foo.csv"));
    }

    @Test
    void blocksArbitraryHost() {
        SecurityException ex = assertThrows(SecurityException.class,
                () -> SsrfGuard.check("https://evil.com/malware.csv"));
        assertTrue(ex.getMessage().contains("allow-list"));
    }

    @Test
    void blocksFileScheme() {
        assertThrows(SecurityException.class, () -> SsrfGuard.check("file:///etc/passwd"));
    }

    @Test
    void blocksBlankUrl() {
        assertThrows(SecurityException.class, () -> SsrfGuard.check(""));
    }

    @Test
    void blocksNullUrl() {
        assertThrows(SecurityException.class, () -> SsrfGuard.check(null));
    }

    @Test
    void isAllowedReturnsFalseForBadHost() {
        assertFalse(SsrfGuard.isAllowed("https://attacker.io/foo.csv"));
    }

    @Test
    void isAllowedReturnsTrueForGoodHost() {
        assertTrue(SsrfGuard.isAllowed("https://repodatos.atdt.gob.mx/foo.csv"));
    }
}
