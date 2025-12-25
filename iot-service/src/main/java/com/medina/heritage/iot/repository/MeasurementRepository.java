package com.medina.heritage.iot.repository;

import com.medina.heritage.iot.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByDeviceIdOrderByTimeDesc(UUID deviceId);
    
    @Query("SELECT m FROM Measurement m WHERE m.deviceId = :deviceId AND m.time BETWEEN :start AND :end ORDER BY m.time DESC")
    List<Measurement> findByDeviceIdAndTimeRange(
        @Param("deviceId") UUID deviceId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}

