package ir.darkdeveloper.anbarinoo.model.Financial;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize
@JsonSerialize
public class FinancialModel {

      @Column(nullable = false, precision = 19, scale = 4)
      private BigDecimal costs;

      @Column(nullable = false, precision = 19, scale = 4)
      private BigDecimal incomes;

      @Column(nullable = false, precision = 19, scale = 4)
      private BigDecimal profit;

      @Column(nullable = false, precision = 19, scale = 4)
      private BigDecimal loss;

      private LocalDateTime fromDate;

      private LocalDateTime toDate;

}
