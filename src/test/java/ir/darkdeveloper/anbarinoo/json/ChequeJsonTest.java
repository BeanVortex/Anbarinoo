package ir.darkdeveloper.anbarinoo.json;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public record ChequeJsonTest(JacksonTester<ChequeModel> jsonTester) {

    @Autowired
    public ChequeJsonTest {
    }

    // testing serialization is not important because the use of DTOs
    @Test
    void serialize() throws IOException {
        var cheque = ChequeModel.builder()
                .id(5L)
                .amount(BigDecimal.TEN)
                .isCheckedOut(true)
                .isDebt(true)
                .nameOf("Me")
                .payTo("Other")
                .user(new UserModel(2L))
                .issuedAt(LocalDateTime.now())
                .validTill(LocalDateTime.now().plusDays(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now().plusSeconds(5))
                .build();
        var json = jsonTester.write(cheque);
        System.out.println(json.getJson());
        assertThat(json).extractingJsonPathValue("$.id").asString().isEqualTo(cheque.getId().toString());
        assertThat(json).extractingJsonPathValue("$.amount").asString().isEqualTo(cheque.getAmount().toString());
        assertThat(json).extractingJsonPathValue("$.isCheckedOut").isEqualTo(cheque.getIsCheckedOut());
        assertThat(json).extractingJsonPathValue("$.isDebt").isEqualTo(cheque.getIsDebt());
        assertThat(json).extractingJsonPathValue("$.nameOf").isEqualTo(cheque.getNameOf());
        assertThat(json).extractingJsonPathValue("$.user").asString().isEqualTo(cheque.getUser().getId().toString());
        assertThat(json).extractingJsonPathValue("$.issuedAt").asString().isEqualTo(cheque.getIssuedAt().toString());
        assertThat(json).extractingJsonPathValue("$.validTill").asString().isEqualTo(cheque.getValidTill().toString());
        assertThat(json).extractingJsonPathValue("$.createdAt").asString().isEqualTo(cheque.getCreatedAt().toString());
        assertThat(json).extractingJsonPathValue("$.updatedAt").asString().isEqualTo(cheque.getUpdatedAt().toString());
    }

    @Test
    void deserialize() throws IOException {
        var cheque = jsonTester.read(new ClassPathResource("json/cheque.json")).getObject();
        assertThat(cheque.getId()).isEqualTo(5);
        assertThat(cheque.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(cheque.getIsCheckedOut()).isEqualTo(true);
        assertThat(cheque.getIsDebt()).isEqualTo(true);
        assertThat(cheque.getIssuedAt().toString()).isEqualTo("2022-03-17T09:56:18.135892362");
        assertThat(cheque.getValidTill().toString()).isEqualTo("2022-03-22T09:56:18.135913398");
        assertThat(cheque.getCreatedAt().toString()).isEqualTo("2022-03-17T09:56:18.135921784");
        assertThat(cheque.getUpdatedAt().toString()).isEqualTo("2022-03-17T09:56:23.135926487");
    }
}
