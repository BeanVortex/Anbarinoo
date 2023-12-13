package ir.darkdeveloper.anbarinoo.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;

import java.io.IOException;

public class DebtOrDemandDeserializer extends StdDeserializer<DebtOrDemandModel> {

    // do not delete
    public DebtOrDemandDeserializer() {
        this(DebtOrDemandModel.class);
    }

    public DebtOrDemandDeserializer(Class<DebtOrDemandModel> t) {
        super(t);
    }

    @Override
    public DebtOrDemandModel deserialize(JsonParser p, DeserializationContext context) throws IOException {
        return JsonUtils.readJson(p, DebtOrDemandModel.class);
    }

}
