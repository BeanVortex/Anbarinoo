package ir.darkdeveloper.anbarinoo.model.Financial;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize
@JsonSerialize
@Builder
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
