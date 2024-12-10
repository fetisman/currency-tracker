package com.fetisman.currencyaggregator.repo;

import com.fetisman.currencymodel.model.CurrencyAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyAggregateRepository extends JpaRepository<CurrencyAggregate, Long> {
    Optional<CurrencyAggregate> findTopByCurrencyCodeOrderByCalculatedAtDesc(String currency);

    List<CurrencyAggregate> findTop2ByCurrencyCodeOrderByCalculatedAtDesc(String currency);

    List<CurrencyAggregate> findByCurrencyCodeAndCalculatedAtAfter(String currency, LocalDateTime timestamp);

    @Query(value = """
    SELECT 
        ROUND(((latest.average_buy::numeric - previous.average_buy::numeric) / previous.average_buy::numeric) * 100, 3) 
    FROM 
        currency_aggregates c1, currency_aggregates c2 
    WHERE 
        c1.currency_code = :currency 
        AND c2.currency_code = :currency 
        AND c1.calculated_at = (SELECT MAX(ca.calculated_at) FROM currency_aggregates ca WHERE ca.currency_code = :currency) 
        AND c2.calculated_at = (
            SELECT MAX(cb.calculated_at) 
            FROM currency_aggregates cb 
            WHERE cb.currency_code = :currency AND cb.calculated_at < c1.calculated_at
        )
""", nativeQuery = true)// it works in console only;
    Optional<Double> computeHourlyDynamics(@Param("currency") String currency);

}

