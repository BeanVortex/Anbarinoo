package ir.darkdeveloper.anbarinoo.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ir.darkdeveloper.anbarinoo.model.Financial.SellsModel;
import lombok.NoArgsConstructor;

import java.io.IOException;

public class SellsSerialize extends StdSerializer<SellsModel> {

    public SellsSerialize() {
        this(null);
    }

    public SellsSerialize(Class<SellsModel> t) {
        super(t);
    }

    @Override
    public void serialize(SellsModel value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartObject();
        if (value.getId() != null)
            gen.writeNumberField("id", value.getId());
        if (value.getCount() != null)
            gen.writeNumberField("count", value.getCount());
        if (value.getPrice() != null)
            gen.writeNumberField("price", value.getPrice());
        if (value.getTax() != null)
            gen.writeNumberField("tax", value.getTax());
        if (value.getProduct() != null)
            gen.writeNumberField("product", value.getProduct().getId());
        if (value.getCreatedAt() != null)
            gen.writeObjectField("createdAt", value.getCreatedAt());
        if (value.getUpdatedAt() != null)
            gen.writeObjectField("updatedAt", value.getUpdatedAt());
        gen.writeEndObject();
    }
}
