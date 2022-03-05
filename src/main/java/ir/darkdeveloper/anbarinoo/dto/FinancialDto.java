package ir.darkdeveloper.anbarinoo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ir.darkdeveloper.anbarinoo.config.StartupConfig;

public record FinancialDto(BigDecimal costs, BigDecimal incomes,
                           BigDecimal profit, BigDecimal loss,
                           String fromDate,
                           String toDate) {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StartupConfig.DATE_FORMAT);


    public FinancialDto(BigDecimal costs, BigDecimal incomes,
                        BigDecimal profit, BigDecimal loss,
                        LocalDateTime fromDate,
                        LocalDateTime toDate) {
        this(costs, incomes, profit, loss,
                fromDate.format(formatter),
                toDate.format(formatter));
    }

    public FinancialDto(BigDecimal costs, BigDecimal incomes, LocalDateTime fromDate, LocalDateTime toDate) {
        this(costs, incomes, null, null,
                fromDate.format(formatter),
                toDate.format(formatter));
    }

    public FinancialDto(LocalDateTime fromDate, LocalDateTime toDate) {
        this(null, null, null, null,
                fromDate.format(formatter),
                toDate.format(formatter));
    }

}
