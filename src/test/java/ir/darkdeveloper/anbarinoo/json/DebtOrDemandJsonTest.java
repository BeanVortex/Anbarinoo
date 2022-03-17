package ir.darkdeveloper.anbarinoo.json;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
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
public record DebtOrDemandJsonTest(JacksonTester<DebtOrDemandModel> jsonTester) {

    @Autowired
    public DebtOrDemandJsonTest {
    }

    // testing serialization is not important because the use of DTOs
    @Test
    void serialize() throws IOException {
        var dod = DebtOrDemandModel.builder()
                .id(5L)
                .amount(BigDecimal.TEN)
                .isCheckedOut(true)
                .isDebt(true)
                .chequeId(16L)
                .nameOf("Me")
                .payTo("Other")
                .user(new UserModel(2L))
                .issuedAt(LocalDateTime.now())
                .validTill(LocalDateTime.now().plusDays(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now().plusSeconds(5))
                .build();
        var json = jsonTester.write(dod);
        System.out.println(json.getJson());
        assertThat(json).extractingJsonPathValue("$.id").asString().isEqualTo(dod.getId().toString());
        assertThat(json).extractingJsonPathValue("$.amount").asString().isEqualTo(dod.getAmount().toString());
        assertThat(json).extractingJsonPathValue("$.isCheckedOut").isEqualTo(dod.getIsCheckedOut());
        assertThat(json).extractingJsonPathValue("$.isDebt").isEqualTo(dod.getIsDebt());
        assertThat(json).extractingJsonPathValue("$.chequeId").asString().isEqualTo(dod.getChequeId().toString());
        assertThat(json).extractingJsonPathValue("$.nameOf").isEqualTo(dod.getNameOf());
        assertThat(json).extractingJsonPathValue("$.user").asString().isEqualTo(dod.getUser().getId().toString());
        assertThat(json).extractingJsonPathValue("$.issuedAt").asString().isEqualTo(dod.getIssuedAt().toString());
        assertThat(json).extractingJsonPathValue("$.validTill").asString().isEqualTo(dod.getValidTill().toString());
        assertThat(json).extractingJsonPathValue("$.createdAt").asString().isEqualTo(dod.getCreatedAt().toString());
        assertThat(json).extractingJsonPathValue("$.updatedAt").asString().isEqualTo(dod.getUpdatedAt().toString());
    }

    @Test
    void deserialize() throws IOException {
        var dod = jsonTester.read(new ClassPathResource("json/dod.json")).getObject();
        assertThat(dod.getId()).isEqualTo(5);
        assertThat(dod.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(dod.getIsCheckedOut()).isEqualTo(true);
        assertThat(dod.getIsDebt()).isEqualTo(true);
        assertThat(dod.getChequeId()).isEqualTo(16L);
        assertThat(dod.getIssuedAt().toString()).isEqualTo("2022-03-17T10:20:24.083856109");
        assertThat(dod.getValidTill().toString()).isEqualTo("2022-03-22T10:20:24.083884601");
        assertThat(dod.getCreatedAt().toString()).isEqualTo("2022-03-17T10:20:24.083893093");
        assertThat(dod.getUpdatedAt().toString()).isEqualTo("2022-03-17T10:20:29.083897826");
    }

}
