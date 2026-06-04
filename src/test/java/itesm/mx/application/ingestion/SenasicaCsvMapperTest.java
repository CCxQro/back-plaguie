package itesm.mx.application.ingestion;

import itesm.mx.infrastructure.ingestion.SenasicaCsvMapper;
import itesm.mx.infrastructure.persistence.entity.vigilancia.VigilanciaFitosanitariaEntity;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CSV row → entity mapping (SCRUM-316 / SCRUM-319).
 */
class SenasicaCsvMapperTest {

    @Test
    void mapsValidRow_toEntity() throws Exception {
        List<CSVRecord> records = loadFixtureRecords();
        assertFalse(records.isEmpty());

        CSVRecord first = records.get(0);
        // Pre-resolved fake catalog IDs
        VigilanciaFitosanitariaEntity entity = SenasicaCsvMapper.toEntity(first, 1L, 2L, 3L, null, 2L);

        assertNotNull(entity, "Valid row should map to non-null entity");
        assertNotNull(entity.latitude);
        assertNotNull(entity.longitude);
        assertNull(entity.validatedAt, "validatedAt must be null (pending validation)");
        assertEquals(2L, entity.statusId, "Status must be Revision (2)");
        assertEquals(1L, entity.plagaId);
        assertEquals(2L, entity.hospedanteId);
        assertEquals(3L, entity.especieId);
    }

    @Test
    void mapsAllFixtureRows_noNull() throws Exception {
        List<CSVRecord> records = loadFixtureRecords();
        long nullCount = records.stream()
                .map(r -> SenasicaCsvMapper.toEntity(r, 1L, 1L, 1L, null, 2L))
                .filter(e -> e == null)
                .count();
        assertEquals(0, nullCount, "All sample rows should map successfully");
    }

    @Test
    void skipsRow_withMissingLatLon() throws Exception {
        String csv = "estado,municipio,plaga,hospedante,especie,variedad,ahosp,lat,lon\n"
                   + "Sonora,Hermosillo,Plaga,Trigo,Esp,,5.0,,\n";
        List<CSVRecord> records = parseCsv(csv);
        VigilanciaFitosanitariaEntity entity = SenasicaCsvMapper.toEntity(records.get(0), 1L, 1L, 1L, null, 2L);
        assertNull(entity, "Row with missing lat/lon should map to null");
    }

    @Test
    void getString_caseInsensitive() throws Exception {
        String csv = "ESTADO,Municipio\nSonora,Hermosillo\n";
        List<CSVRecord> records = parseCsv(csv);
        String val = SenasicaCsvMapper.getString(records.get(0), "estado");
        assertEquals("Sonora", val);
    }

    @Test
    void ahospDefaultsToZero_whenMissing() throws Exception {
        String csv = "estado,municipio,plaga,hospedante,especie,variedad,ahosp,lat,lon\n"
                   + "Sonora,Hermosillo,Plaga,Trigo,Esp,Var,,20.0,-100.0\n";
        List<CSVRecord> records = parseCsv(csv);
        VigilanciaFitosanitariaEntity entity = SenasicaCsvMapper.toEntity(records.get(0), 1L, 1L, 1L, null, 2L);
        assertNotNull(entity);
        assertEquals(0, entity.ahosp.compareTo(java.math.BigDecimal.ZERO));
    }

    private List<CSVRecord> loadFixtureRecords() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("fixtures/senasica_sample.csv")) {
            assertNotNull(is);
            return parseCsv(new InputStreamReader(is, StandardCharsets.UTF_8));
        }
    }

    private List<CSVRecord> parseCsv(String csv) throws Exception {
        return parseCsv(new java.io.StringReader(csv));
    }

    private List<CSVRecord> parseCsv(Reader reader) throws Exception {
        try (CSVParser parser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build()
                .parse(reader)) {
            return parser.getRecords();
        }
    }
}
