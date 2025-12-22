package com.medina.heritage.patrimoine.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String address;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "geometry(Point,4326)")
  private Point geom;

  @Column(name = "sf_asset_id")
  private String sfAssetId;

  @Column(name = "image_url", columnDefinition = "TEXT")
  private String imageUrl;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;


}