package com.medina.heritage.iot.repository;

import com.medina.heritage.iot.entity.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, Integer> {
    List<RiskRule> findByMetricType(String metricType);
    Optional<RiskRule> findByMetricTypeAndSeverityLevel(String metricType, String severityLevel);
}

