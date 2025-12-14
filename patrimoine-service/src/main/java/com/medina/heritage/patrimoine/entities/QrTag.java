package com.medina.heritage.patrimoine.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrTag {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "qr_content", unique = true, nullable = false)
  private String qrContent;

  @Builder.Default
  @Column(length = 20)
  private String status = "ACTIVE"; 

  @CreationTimestamp
  @Column(name = "installed_at", updatable = false)
  private LocalDateTime installedAt;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "building_id", nullable = false)
  private Building building;
}