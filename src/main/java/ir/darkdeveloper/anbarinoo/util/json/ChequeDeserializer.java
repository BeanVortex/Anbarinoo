package ir.darkdeveloper.anbarinoo.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;

import java.io.IOException;

public class ChequeDeserializer extends StdDeserializer<ChequeModel> {

    // do not delete
    public ChequeDeserializer() {
        this(ChequeModel.class);
    }

    public ChequeDeserializer(Class<ChequeModel> t) {
        super(t);
    }

    @Override
    public ChequeModel deserialize(JsonParser p, DeserializationContext context) throws IOException {
        return JsonUtils.readJson(p, ChequeModel.class);
    }
}
