package com.medina.heritage.patrimoine.repositories;

import com.medina.heritage.patrimoine.entities.QrTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface QrTagRepository extends JpaRepository<QrTag, UUID> {
  Optional<QrTag> findByQrContent(String qrContent);
}