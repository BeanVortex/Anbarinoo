package ir.darkdeveloper.anbarinoo.model.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import lombok.SneakyThrows;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChequeDeserializer extends StdDeserializer<ChequeModel> {

    // do not delete
    public ChequeDeserializer() {
        this(null);
    }

    public ChequeDeserializer(Class<ChequeModel> t) {
        super(t);
    }


    @SneakyThrows
    @Override
    public ChequeModel deserialize(JsonParser p, DeserializationContext context) {

        JsonNode node = p.getCodec().readTree(p);

        var id = node.get("id") != null ? node.get("id").longValue() : null;
        var nameOf = node.get("nameOf") != null ? node.get("nameOf").asText() : null;
        var payTo = node.get("payTo") != null ? node.get("payTo").asText() : null;
        var amount = node.get("amount") != null ? node.get("amount").decimalValue() : null;
        var isDebt = node.get("isDebt") != null ? node.get("isDebt").booleanValue() : null;
        var isCheckedOut = node.get("isCheckedOut") != null ? node.get("isCheckedOut").booleanValue() : null;
        var userId = node.get("user") != null ? node.get("user").longValue() : null;
        if (userId == null)
            userId = node.get("userId") != null ? node.get("userId").longValue() : null;
        var user = new UserModel(userId);


        var dateFormatter = DateTimeFormatter.ofPattern("EE MMM dd yyyy HH:mm:ss");
        var issuedAt = (LocalDateTime) null;
        if (node.get("issuedAt") != null) {
            var issuedAtStr = node.get("issuedAt").asText();
            if (isLegalDate(issuedAtStr))
                issuedAt = LocalDateTime.parse(issuedAtStr, dateFormatter);
            else
                issuedAt = LocalDateTime.parse(issuedAtStr);
        }

        var validTill = (LocalDateTime) null;
        if (node.get("validTill") != null) {
            var validTillStr = node.get("validTill").asText();
            if (isLegalDate(validTillStr))
                validTill = LocalDateTime.parse(validTillStr, dateFormatter);
            else
                validTill = LocalDateTime.parse(validTillStr);
        }

        var createdAt = (LocalDateTime) null;
        if (node.get("createdAt") != null) {
            var createdAtString = node.get("createdAt").asText();
            if (isLegalDate(createdAtString))
                createdAt = LocalDateTime.parse(createdAtString, dateFormatter);
            else
                createdAt = LocalDateTime.parse(createdAtString);
        }

        var updatedAt = (LocalDateTime) null;
        if (node.get("updatedAt") != null) {
            var updatedAtString = node.get("updatedAt").asText();
            if (isLegalDate(updatedAtString))
                updatedAt = LocalDateTime.parse(updatedAtString, dateFormatter);
            else
                updatedAt = LocalDateTime.parse(updatedAtString);
        }
        return new ChequeModel(id, nameOf, payTo, amount, isDebt, isCheckedOut, user, issuedAt, validTill,
                createdAt, updatedAt);
    }

    boolean isLegalDate(String s) {
        var sdf = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");
        sdf.setLenient(false);
        return sdf.parse(s, new ParsePosition(0)) != null;
    }
}
