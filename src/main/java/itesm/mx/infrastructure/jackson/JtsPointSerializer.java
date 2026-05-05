package itesm.mx.infrastructure.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

/**
 * JTS {@link Point} is a {@link org.locationtech.jts.geom.Geometry}; Jackson's default
 * bean serialization follows getters like {@code getEnvelope()}, which returns nested
 * geometries and produces infinite JSON nesting under {@code envelope}.
 */
public class JtsPointSerializer extends JsonSerializer<Point> {

    @Override
    public void serialize(Point value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeStartObject();
        gen.writeNumberField("longitude", value.getX());
        gen.writeNumberField("latitude", value.getY());
        gen.writeEndObject();
    }
}
