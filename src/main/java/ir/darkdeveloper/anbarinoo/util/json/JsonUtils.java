package ir.darkdeveloper.anbarinoo.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import ir.darkdeveloper.anbarinoo.config.StartupConfig;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class JsonUtils {
    public static void writeJson(JsonGenerator gen, Long id, String nameOf, String payTo,
                                 BigDecimal amount, Boolean isDebt, Boolean isCheckedOut,
                                 UserModel user, LocalDateTime issuedAt, LocalDateTime validTill,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) throws IOException {
        if (id != null)
            gen.writeNumberField("id", id);
        if (nameOf != null)
            gen.writeStringField("nameOf", nameOf);
        if (payTo != null)
            gen.writeStringField("payTo", payTo);
        if (amount != null)
            gen.writeNumberField("amount", amount);
        if (isDebt != null)
            gen.writeBooleanField("isDebt", isDebt);
        if (isCheckedOut != null)
            gen.writeBooleanField("isCheckedOut", isCheckedOut);
        if (user != null)
            gen.writeNumberField("user", user.getId());
        if (issuedAt != null)
            gen.writeStringField("issuedAt", issuedAt.toString());
        if (validTill != null)
            gen.writeStringField("validTill", validTill.toString());
        if (createdAt != null)
            gen.writeStringField("createdAt", createdAt.toString());
        if (updatedAt != null)
            gen.writeStringField("updatedAt", updatedAt.toString());
    }

    public static <T> T readJson(JsonParser p, Class<T> clazz) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        var id = node.get("id") != null ? node.get("id").longValue() : null;
        var nameOf = node.get("nameOf") != null ? node.get("nameOf").asText() : null;
        var payTo = node.get("payTo") != null ? node.get("payTo").asText() : null;
        var isDebt = node.get("isDebt") != null ? node.get("isDebt").booleanValue() : null;
        var isCheckedOut = node.get("isCheckedOut") != null ? node.get("isCheckedOut").booleanValue() : null;
        var amount = node.get("amount") != null ? node.get("amount").decimalValue() : null;
        var userId = node.get("user") != null ? node.get("user").longValue() : null;
        if (userId == null)
            userId = node.get("userId") != null ? node.get("userId").longValue() : null;
        var user = new UserModel(userId);

        var dateFormatter = StartupConfig.DATE_FORMATTER;

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

        T t = null;

        if (clazz == DebtOrDemandModel.class) {
            var chequeId = node.get("chequeId") != null ? node.get("chequeId").longValue() : null;
            t = clazz.cast(new DebtOrDemandModel(id, nameOf, payTo, isDebt, isCheckedOut, amount, chequeId, user,
                    issuedAt, validTill, createdAt, updatedAt));
        }
        if (clazz == ChequeModel.class)
            t = clazz.cast(new ChequeModel(id, nameOf, payTo, amount, isDebt, isCheckedOut, user, issuedAt, validTill,
                    createdAt, updatedAt));

        return t;

    }

    private static boolean isLegalDate(String s) {
        var sdf = new SimpleDateFormat(StartupConfig.DATE_FORMAT);
        sdf.setLenient(false);
        return sdf.parse(s, new ParsePosition(0)) != null;
    }
}
