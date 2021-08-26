package ir.darkdeveloper.anbarinoo.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;

import java.io.IOException;

public class SellSerialize extends StdSerializer<SellModel> {

    public SellSerialize() {
        this(null);
    }

    public SellSerialize(Class<SellModel> t) {
        super(t);
    }

    @Override
    public void serialize(SellModel value, JsonGenerator gen, SerializerProvider provider) throws IOException {

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
            gen.writeStringField("createdAt", value.getCreatedAt().toString());
        if (value.getUpdatedAt() != null)
            gen.writeStringField("updatedAt", value.getUpdatedAt().toString());
        gen.writeEndObject();
    }
}