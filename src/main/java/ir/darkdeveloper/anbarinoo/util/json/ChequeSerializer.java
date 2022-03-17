package ir.darkdeveloper.anbarinoo.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChequeSerializer extends StdSerializer<ChequeModel> {

    public ChequeSerializer() {
        this(ChequeModel.class);
    }

    public ChequeSerializer(Class<ChequeModel> t) {
        super(t);
    }

    @Override
    public void serialize(ChequeModel value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        JsonUtils.writeJson(gen, value.getId(), value.getNameOf(), value.getPayTo(), value.getAmount(),
                value.getIsDebt(), value.getIsCheckedOut(), value.getUser(),
                value.getIssuedAt(), value.getValidTill(), value.getCreatedAt(),
                value.getUpdatedAt());
        gen.writeEndObject();
    }


}
