package ir.darkdeveloper.anbarinoo.model.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ir.darkdeveloper.anbarinoo.model.Financial.SellsModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;

import java.io.IOException;
import java.time.LocalDateTime;

public class SellsBuysDeserializer extends StdDeserializer<SellsModel> {
    public SellsBuysDeserializer() {
        this(null);
    }

    public SellsBuysDeserializer(Class<SellsModel> t) {
        super(t);
    }

    @Override
    public SellsModel deserialize(JsonParser p, DeserializationContext context) throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        var id = node.get("id") != null ? node.get("id").longValue() : null;
        var count = node.get("count") != null ? node.get("count").decimalValue() : null;
        var price = node.get("price") != null ? node.get("price").decimalValue() : null;
        var tax = node.get("tax") != null ? node.get("tax").intValue() : null;
        var productId = node.get("product") != null ? node.get("product").longValue() : null;
        var product = new ProductModel(productId);

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

        return new SellsModel(id, count, price, tax, product, createdAt, updatedAt);

    }
}
