package ir.darkdeveloper.anbarinoo.model.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;

import java.io.IOException;
import java.time.LocalDateTime;

public class DebtOrDemandDeserializer extends StdDeserializer<DebtOrDemandModel> {

    public DebtOrDemandDeserializer() {
        this(null);
    }

    public DebtOrDemandDeserializer(Class<DebtOrDemandModel> t) {
        super(t);
    }

    @Override
    public DebtOrDemandModel deserialize(JsonParser p, DeserializationContext context) throws IOException {


        JsonNode node = p.getCodec().readTree(p);
        var id = node.get("id") != null ? node.get("id").longValue() : null;
        var nameOf = node.get("nameOf") != null ? node.get("nameOf").asText() : null;
        var payTo = node.get("payTo") != null ? node.get("payTo").asText() : null;
        var isDebt = node.get("isDebt") != null ? node.get("isDebt").booleanValue() : null;
        var isCheckedOut = node.get("isCheckedOut") != null ? node.get("isCheckedOut").booleanValue() : null;
        var amount = node.get("amount") != null ? node.get("amount").decimalValue() : null;
        var chequeId = node.get("chequeId") != null ? node.get("chequeId").longValue() : null;
        var userId = node.get("user") != null ? node.get("user").longValue() : null;
        var user = new UserModel(userId);


        var issuedAt = (LocalDateTime) null;
        if (node.get("issuedAt") != null) {
            var issuedAtStr = node.get("issuedAt").asText();
            issuedAt = LocalDateTime.parse(issuedAtStr);
        }

        var validTill = (LocalDateTime) null;
        if (node.get("validTill") != null) {
            var validTillStr = node.get("validTill").asText();
            validTill = LocalDateTime.parse(validTillStr);
        }

        var createdAt = (LocalDateTime) null;
        if (node.get("createdAt") != null) {
            var createdAtString = node.get("createdAt").asText();
            createdAt = LocalDateTime.parse(createdAtString);
        }

        var updatedAt = (LocalDateTime) null;
        if (node.get("updatedAt") != null) {
            var updatedAtString = node.get("updatedAt").asText();
            updatedAt = LocalDateTime.parse(updatedAtString);
        }

        return new DebtOrDemandModel(id, nameOf, payTo, isDebt, isCheckedOut, amount, chequeId, user,
                issuedAt, validTill, createdAt, updatedAt);

    }


}
