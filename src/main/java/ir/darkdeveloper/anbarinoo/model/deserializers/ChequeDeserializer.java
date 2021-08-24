package ir.darkdeveloper.anbarinoo.model.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;

import java.io.IOException;
import java.time.LocalDateTime;

public class ChequeDeserializer extends StdDeserializer<ChequeModel> {

    public ChequeDeserializer() {
        this(null);
    }

    public ChequeDeserializer(Class<ChequeModel> t) {
        super(t);
    }


    @Override
    public ChequeModel deserialize(JsonParser p, DeserializationContext context) throws IOException {

        JsonNode node = p.getCodec().readTree(p);

        var id = node.get("id") != null ? node.get("id").longValue() : null;
        var nameOf = node.get("nameOf") != null ? node.get("nameOf").asText() : null;
        var payTo = node.get("payTo") != null ? node.get("payTo").asText() : null;
        var amount = node.get("amount") != null ? node.get("amount").decimalValue() : null;
        var isDebt = node.get("isDebt") != null ? node.get("isDebt").booleanValue() : null;
        var isCheckedOut = node.get("isCheckedOut") != null ? node.get("isCheckedOut").booleanValue() : null;
        var userId = node.get("user") != null ? node.get("user").longValue() : null;
        var user = new UserModel(userId);
        var issuedAtStr = node.get("issuedAt") != null ? node.get("issuedAt").asText() : null;
        var validTillStr = node.get("validTill") != null ? node.get("validTill").asText() : null;
        var createdAtStr = node.get("createdAt") != null ? node.get("createdAt").asText() : null;
        var updatedAtStr = node.get("updatedAt") != null ? node.get("updatedAt").asText() : null;

        var issuedAt = (LocalDateTime) null;
        if (issuedAtStr != null)
            issuedAt = LocalDateTime.parse(issuedAtStr);

        var validTill = (LocalDateTime) null;
        if (validTillStr != null)
            validTill = LocalDateTime.parse(validTillStr);

        var createdAt = (LocalDateTime) null;
        if (createdAtStr != null)
            createdAt = LocalDateTime.parse(createdAtStr);

        var updatedAt = (LocalDateTime) null;
        if (updatedAtStr != null)
            updatedAt = LocalDateTime.parse(updatedAtStr);

        return new ChequeModel(id, nameOf, payTo, amount, isDebt, isCheckedOut, user, issuedAt, validTill,
                createdAt, updatedAt);
    }
}
