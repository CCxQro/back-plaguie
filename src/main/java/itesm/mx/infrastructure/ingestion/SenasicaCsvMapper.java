package itesm.mx.infrastructure.ingestion;

import itesm.mx.infrastructure.persistence.entity.vigilancia.VigilanciaFitosanitariaEntity;
import org.apache.commons.csv.CSVRecord;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

/**
 * Maps a single Apache Commons CSV record to a {@link VigilanciaFitosanitariaEntity}.
 *
 * CSV column mapping (documented expected headers from SENASICA dataset):
 * TODO: Confirm exact headers by downloading a real file from
 *       https://repodatos.atdt.gob.mx/api_update/senasica/programas_vigilancia_epidemiologica_fitosanitaria/
 *       The mapping below is based on documented schema (see plan §4.4) and the fixture CSV
 *       at src/test/resources/fixtures/senasica_sample.csv.
 *
 * Expected headers (case-insensitive matching used):
 *   estado, municipio, plaga, hospedante, especie, variedad, ahosp, lat, lon (or latitud, longitud)
 */
public final class SenasicaCsvMapper {

    private static final Logger LOG = Logger.getLogger(SenasicaCsvMapper.class);

    // Expected CSV column names — adjust if real CSV uses different casing/spelling
    public static final String COL_ESTADO    = "estado";
    public static final String COL_MUNICIPIO = "municipio";
    public static final String COL_PLAGA     = "plaga";
    public static final String COL_HOSPEDANTE = "hospedante";
    public static final String COL_ESPECIE   = "especie";
    public static final String COL_VARIEDAD  = "variedad";
    public static final String COL_AHOSP     = "ahosp";
    public static final String COL_LAT       = "lat";
    public static final String COL_LON       = "lon";
    // Alternative lat/lon names seen in some exports
    public static final String COL_LATITUD   = "latitud";
    public static final String COL_LONGITUD  = "longitud";
    public static final String COL_LATITUDE  = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    private SenasicaCsvMapper() {
    }

    /**
     * Maps a CSV record to a VigilanciaFitosanitariaEntity using pre-resolved catalog IDs.
     * The entity's validatedAt is left null (pending admin validation per HU-12).
     *
     * @param record        the CSV record
     * @param plagaId       pre-resolved plaga ID (may be null)
     * @param hospedanteId  pre-resolved hospedante ID (may be null)
     * @param especieId     pre-resolved especie ID (may be null)
     * @param statusId      the "pending" status ID (2L = Revision)
     * @return entity ready for persistence, or null if row is unparseable/invalid
     */
    public static VigilanciaFitosanitariaEntity toEntity(
            CSVRecord record,
            Long plagaId,
            Long hospedanteId,
            Long especieId,
            Long variedadId,
            Long statusId) {
        try {
            BigDecimal lat = parseDecimal(record, COL_LAT, COL_LATITUD, COL_LATITUDE);
            BigDecimal lon = parseDecimal(record, COL_LON, COL_LONGITUD, COL_LONGITUDE);

            if (lat == null || lon == null) {
                LOG.debugf("Skipping row %d: missing lat/lon", record.getRecordNumber());
                return null;
            }

            BigDecimal ahosp = parseDecimal(record, COL_AHOSP);
            if (ahosp == null) ahosp = BigDecimal.ZERO;

            VigilanciaFitosanitariaEntity entity = new VigilanciaFitosanitariaEntity();
            entity.latitude = lat;
            entity.longitude = lon;
            entity.ahosp = ahosp;
            entity.plagaId = plagaId;
            entity.hospedanteId = hospedanteId;
            entity.especieId = especieId;
            entity.variedadId = variedadId;
            entity.statusId = statusId;
            entity.validatedAt = null; // pending — must go through admin validation (HU-12)
            return entity;
        } catch (Exception e) {
            LOG.warnf("Failed to map CSV row %d: %s", record.getRecordNumber(), e.getMessage());
            return null;
        }
    }

    /**
     * Reads a CSV field value, trying multiple possible column names (case-insensitive).
     * Returns null if none found or blank.
     */
    public static String getString(CSVRecord record, String... columnNames) {
        for (String col : columnNames) {
            try {
                if (record.isMapped(col)) {
                    String val = record.get(col);
                    if (val != null && !val.isBlank()) return val.trim();
                }
                // Try case-insensitive — CSVFormat.withHeader already handles case if set that way
                // Check all headers in the record for a case-insensitive match
                for (String header : record.getParser().getHeaderNames()) {
                    if (header.equalsIgnoreCase(col)) {
                        String val = record.get(header);
                        if (val != null && !val.isBlank()) return val.trim();
                    }
                }
            } catch (Exception ignored) {
                // column not present, try next
            }
        }
        return null;
    }

    private static BigDecimal parseDecimal(CSVRecord record, String... columnNames) {
        String val = getString(record, columnNames);
        if (val == null) return null;
        try {
            return new BigDecimal(val.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
