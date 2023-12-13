package ir.darkdeveloper.anbarinoo.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.util.json.ChequeSerializer;

import java.io.IOException;

public class DebtOrDemandSerializer extends StdSerializer<DebtOrDemandModel> {

    public DebtOrDemandSerializer() {
        this(DebtOrDemandModel.class);
    }
    public DebtOrDemandSerializer(Class<DebtOrDemandModel> t) {
        super(t);
    }

    @Override
    public void serialize(DebtOrDemandModel value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        JsonUtils.writeJson(gen, value.getId(), value.getNameOf(), value.getPayTo(),
                value.getAmount(), value.getIsDebt(), value.getIsCheckedOut(),
                value.getUser(), value.getIssuedAt(), value.getValidTill(),
                value.getCreatedAt(), value.getUpdatedAt());
        if (value.getChequeId() != null)
            gen.writeNumberField("chequeId", value.getChequeId());
        gen.writeEndObject();
    }


}
