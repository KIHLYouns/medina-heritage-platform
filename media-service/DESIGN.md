# 📸 Media Service - Conception MVP

## 1. Vue d'ensemble

### 1.1 Objectif
Service de gestion des médias (images, vidéos, documents) pour la plateforme Medina Heritage. 
Stockage sur **AWS S3** avec métadonnées en PostgreSQL.

### 1.2 Responsabilités
- Upload sécurisé de fichiers vers S3
- Génération d'URLs signées (accès temporaire sécurisé)
- Gestion des métadonnées (type, taille, propriétaire)
- Association des médias aux entités (signalements, bâtiments)
- Suppression logique et physique des fichiers

### 1.3 Intégrations
```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  User-Auth-Svc  │────▶│   Media-Svc     │────▶│    AWS S3       │
│  (userId)       │     │                 │     │   (stockage)    │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        ▼                        ▼                        ▼
┌───────────────┐     ┌─────────────────┐     ┌───────────────────┐
│ Patrimoine-Svc│     │ Salesforce-Svc  │     │   Frontend/App    │
│ (buildingId)  │     │ (Einstein IA)   │     │ (presigned URLs)  │
└───────────────┘     └─────────────────┘     └───────────────────┘
```

---

## 2. Architecture

### 2.1 Stack Technique
| Composant | Technologie |
|-----------|-------------|
| Framework | Spring Boot 4.0 |
| Stockage Cloud | AWS S3 |
| SDK AWS | AWS SDK v2 (software.amazon.awssdk) |
| Base de données | PostgreSQL |
| Validation | Jakarta Validation |

### 2.2 Structure des Packages
```
com.medina.heritage.media
├── MediaApplication.java
├── config/
│   ├── S3Config.java              # Configuration AWS S3
│   └── CorsConfig.java            # Configuration CORS
├── controller/
│   └── MediaController.java       # API REST
├── dto/
│   ├── request/
│   │   └── UploadMediaRequest.java
│   └── response/
│       ├── MediaResponse.java
│       ├── PresignedUrlResponse.java
│       └── ApiResponse.java
├── entity/
│   └── MediaFile.java
├── enums/
│   ├── MediaType.java             # IMAGE, VIDEO, DOCUMENT
│   ├── MediaStatus.java           # ACTIVE, DELETED
│   └── EntityType.java            # REPORT, BUILDING, USER
├── exception/
│   ├── MediaNotFoundException.java
│   ├── FileUploadException.java
│   ├── InvalidFileTypeException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   └── MediaFileRepository.java
├── service/
│   ├── MediaService.java          # Logique métier
│   ├── S3StorageService.java      # Opérations S3
│   └── FileValidationService.java # Validation fichiers
└── mapper/
    └── MediaMapper.java
```

---

## 3. Modèle de Données

### 3.1 Entité MediaFile

```java
@Entity
@Table(name = "media_files")
public class MediaFile {
    
    @Id
    private UUID id;
    
    // Propriétaire du fichier (User)
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    // Entité associée (optionnel)
    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private EntityType entityType;  // REPORT, BUILDING, USER_AVATAR
    
    @Column(name = "entity_id")
    private UUID entityId;
    
    // Informations S3
    @Column(name = "bucket_name", nullable = false, length = 100)
    private String bucketName;
    
    @Column(name = "file_key", nullable = false, length = 500)
    private String fileKey;         // ex: reports/2025/12/uuid-filename.jpg
    
    // Métadonnées fichier
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;        // image/jpeg, video/mp4
    
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;    // IMAGE, VIDEO, DOCUMENT
    
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;
    
    // État
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MediaStatus status = MediaStatus.ACTIVE;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
```

### 3.2 Schéma SQL Révisé

```sql
-- Service: MediaService

CREATE TABLE IF NOT EXISTS media_files (
    id UUID PRIMARY KEY,
    
    -- Propriétaire
    user_id UUID NOT NULL,
    
    -- Association (optionnelle)
    entity_type VARCHAR(50),        -- 'REPORT', 'BUILDING', 'USER_AVATAR'
    entity_id UUID,
    
    -- Stockage S3
    bucket_name VARCHAR(100) NOT NULL,
    file_key VARCHAR(500) NOT NULL UNIQUE,
    
    -- Métadonnées fichier
    original_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    media_type VARCHAR(20),         -- 'IMAGE', 'VIDEO', 'DOCUMENT'
    file_size_bytes BIGINT,
    
    -- État
    status VARCHAR(20) DEFAULT 'ACTIVE',
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Index pour recherche rapide
CREATE INDEX IF NOT EXISTS idx_media_user_id ON media_files(user_id);
CREATE INDEX IF NOT EXISTS idx_media_entity ON media_files(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_media_status ON media_files(status);
```

### 3.3 Enums

```java
public enum MediaType {
    IMAGE,      // jpg, png, webp, gif
    VIDEO,      // mp4, mov, webm
    DOCUMENT    // pdf, doc, docx
}

public enum MediaStatus {
    ACTIVE,     // Fichier actif et accessible
    DELETED     // Supprimé logiquement (conservé X jours avant purge)
}

public enum EntityType {
    REPORT,         // Signalement citoyen
    BUILDING,       // Photo de bâtiment patrimoine
    USER_AVATAR,    // Avatar utilisateur
    INSPECTION      // Rapport d'inspection technique
}
```

---

## 4. API REST

### 4.1 Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/media/upload` | Upload un fichier |
| `POST` | `/api/media/upload/presigned` | Génère une URL d'upload présignée |
| `GET` | `/api/media/{id}` | Récupère les métadonnées d'un fichier |
| `GET` | `/api/media/{id}/url` | Génère une URL de téléchargement présignée |
| `GET` | `/api/media/entity/{type}/{id}` | Liste les médias d'une entité |
| `GET` | `/api/media/user/{userId}` | Liste les médias d'un utilisateur |
| `DELETE` | `/api/media/{id}` | Suppression logique |
| `DELETE` | `/api/media/{id}/permanent` | Suppression physique (admin) |

### 4.2 DTOs

#### UploadMediaRequest
```java
@Data
public class UploadMediaRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private EntityType entityType;  // Optionnel
    private UUID entityId;          // Optionnel
    
    // Le fichier est envoyé en MultipartFile, pas dans le DTO
}
```

#### MediaResponse
```java
@Data
public class MediaResponse {
    private UUID id;
    private UUID userId;
    private EntityType entityType;
    private UUID entityId;
    private String originalFilename;
    private String mimeType;
    private MediaType mediaType;
    private Long fileSizeBytes;
    private MediaStatus status;
    private OffsetDateTime createdAt;
    
    // URL présignée (optionnelle, générée à la demande)
    private String downloadUrl;
    private OffsetDateTime urlExpiresAt;
}
```

#### PresignedUrlResponse
```java
@Data
public class PresignedUrlResponse {
    private String uploadUrl;       // URL S3 présignée pour PUT
    private String fileKey;         // Clé S3 à utiliser
    private OffsetDateTime expiresAt;
    private Map<String, String> requiredHeaders; // Headers à inclure
}
```

---

## 5. Services

### 5.1 S3StorageService

```java
@Service
@RequiredArgsConstructor
public class S3StorageService {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    /**
     * Upload un fichier vers S3.
     * @return la clé S3 du fichier uploadé
     */
    public String uploadFile(MultipartFile file, String fileKey);
    
    /**
     * Génère une URL présignée pour upload direct (client → S3).
     * Durée de validité : 15 minutes.
     */
    public PresignedUrlResponse generateUploadUrl(String fileKey, String contentType);
    
    /**
     * Génère une URL présignée pour téléchargement.
     * Durée de validité : configurable (défaut 1 heure).
     */
    public String generateDownloadUrl(String fileKey, Duration expiration);
    
    /**
     * Supprime un fichier de S3.
     */
    public void deleteFile(String fileKey);
    
    /**
     * Vérifie si un fichier existe dans S3.
     */
    public boolean fileExists(String fileKey);
}
```

### 5.2 FileValidationService

```java
@Service
public class FileValidationService {
    
    // Tailles maximales par type
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;  // 10 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100 MB
    private static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20 MB
    
    // Types MIME autorisés
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
        "video/mp4", "video/quicktime", "video/webm"
    );
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
        "application/pdf", 
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    
    /**
     * Valide un fichier (type, taille, contenu).
     * @throws InvalidFileTypeException si le type n'est pas autorisé
     * @throws FileUploadException si la taille dépasse la limite
     */
    public void validateFile(MultipartFile file);
    
    /**
     * Détermine le MediaType à partir du MIME type.
     */
    public MediaType determineMediaType(String mimeType);
    
    /**
     * Génère une clé S3 unique et organisée.
     * Format: {entityType}/{année}/{mois}/{uuid}-{filename}
     * Exemple: reports/2025/12/a1b2c3d4-photo.jpg
     */
    public String generateFileKey(String originalFilename, EntityType entityType);
}
```

### 5.3 MediaService

```java
@Service
@RequiredArgsConstructor
public class MediaService {
    
    private final MediaFileRepository mediaFileRepository;
    private final S3StorageService s3StorageService;
    private final FileValidationService fileValidationService;
    private final MediaMapper mediaMapper;
    
    /**
     * Upload un fichier via le serveur (client → backend → S3).
     */
    @Transactional
    public MediaResponse uploadFile(MultipartFile file, UploadMediaRequest request);
    
    /**
     * Génère une URL présignée pour upload direct (client → S3).
     * Crée un enregistrement "pending" en base.
     */
    @Transactional
    public PresignedUrlResponse generatePresignedUploadUrl(UploadMediaRequest request, 
                                                            String filename, 
                                                            String contentType);
    
    /**
     * Confirme qu'un upload direct a réussi.
     * Met à jour le statut et les métadonnées.
     */
    @Transactional
    public MediaResponse confirmUpload(UUID mediaId, long fileSize);
    
    /**
     * Récupère un média par ID.
     */
    public MediaResponse getMediaById(UUID id);
    
    /**
     * Génère une URL de téléchargement présignée.
     */
    public MediaResponse getMediaWithDownloadUrl(UUID id, Duration urlExpiration);
    
    /**
     * Liste les médias associés à une entité.
     */
    public List<MediaResponse> getMediaByEntity(EntityType entityType, UUID entityId);
    
    /**
     * Liste les médias d'un utilisateur.
     */
    public List<MediaResponse> getMediaByUser(UUID userId);
    
    /**
     * Suppression logique (soft delete).
     */
    @Transactional
    public void deleteMedia(UUID id, UUID requestingUserId);
    
    /**
     * Suppression physique (admin seulement).
     * Supprime de S3 + base de données.
     */
    @Transactional
    public void permanentlyDeleteMedia(UUID id);
}
```

---

## 6. Configuration AWS S3

### 6.1 S3Config

```java
@Configuration
public class S3Config {
    
    @Value("${aws.region:eu-west-3}")
    private String region;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;
    
    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region));
        
        // Si credentials explicites (dev), sinon utilise la chaîne par défaut (IAM, env vars)
        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        }
        
        return builder.build();
    }
    
    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region));
        
        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        }
        
        return builder.build();
    }
}
```

### 6.2 application.properties

```properties
spring.application.name=media-service
server.port=${MEDIA_SERVICE_PORT:8084}

# Base de données
spring.datasource.url=${MEDIA_DATABASE_URL:jdbc:postgresql://localhost:5432/heritage_db}
spring.datasource.username=${MEDIA_DATABASE_USERNAME:postgres}
spring.datasource.password=${MEDIA_DATABASE_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=${JPA_HIBERNATE_DDL_AUTO:update}
spring.sql.init.mode=${SPRING_SQL_INIT_MODE:always}

# AWS S3 Configuration
aws.region=${AWS_REGION:eu-west-3}
aws.s3.bucket-name=${AWS_S3_BUCKET_NAME:medina-heritage-media}

# Credentials (optionnel - utiliser IAM roles en production)
aws.access-key-id=${AWS_ACCESS_KEY_ID:}
aws.secret-access-key=${AWS_SECRET_ACCESS_KEY:}

# URLs présignées
aws.s3.presigned-url.upload-expiration-minutes=15
aws.s3.presigned-url.download-expiration-minutes=60

# Upload limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# CORS
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

---

## 7. Gestion des Erreurs

### 7.1 Exceptions Personnalisées

```java
public class MediaNotFoundException extends RuntimeException {
    public MediaNotFoundException(UUID id) {
        super("Media not found with id: " + id);
    }
}

public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String mimeType) {
        super("File type not allowed: " + mimeType);
    }
}

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException(long size, long maxSize) {
        super(String.format("File size %d exceeds maximum allowed size %d", size, maxSize));
    }
}
```

### 7.2 GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaNotFound(MediaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFileType(InvalidFileTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileSizeExceeded(FileSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileUpload(FileUploadException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("File upload failed: " + ex.getMessage()));
    }
    
    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleS3Error(SdkClientException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Storage service unavailable"));
    }
}
```

---

## 8. Sécurité

### 8.1 Contrôle d'Accès
- **Upload** : Tout utilisateur authentifié peut uploader
- **Lecture** : Propriétaire du fichier OU fichier associé à une entité publique
- **Suppression** : Propriétaire uniquement (soft delete)
- **Suppression permanente** : Admin uniquement

### 8.2 Validation des Fichiers
```
1. Vérification du type MIME (Content-Type header)
2. Vérification de l'extension du fichier
3. Vérification de la signature magique (magic bytes) - Anti-spoofing
4. Scan antivirus (optionnel, via ClamAV en production)
```

### 8.3 URLs Présignées
- **Upload** : Expire après 15 minutes
- **Download** : Expire après 1 heure (configurable)
- Les URLs contiennent une signature cryptographique AWS

### 8.4 Structure des Clés S3
```
medina-heritage-media/
├── reports/
│   └── 2025/
│       └── 12/
│           └── {uuid}-{filename}.jpg
├── buildings/
│   └── 2025/
│       └── 12/
│           └── {uuid}-{filename}.jpg
├── avatars/
│   └── {userId}/
│       └── avatar.jpg
└── inspections/
    └── 2025/
        └── 12/
            └── {uuid}-{filename}.pdf
```

---

## 9. Dépendances Maven

```xml
<!-- AWS SDK v2 pour S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.29.0</version>
</dependency>

<!-- Presigned URLs -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3-transfer-manager</artifactId>
    <version>2.29.0</version>
</dependency>

<!-- BOM AWS (gestion des versions) -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>bom</artifactId>
            <version>2.29.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 10. Flux de Données

### 10.1 Upload via Backend (Simple)
```
┌────────┐     ┌──────────────┐     ┌─────────┐
│ Client │────▶│ Media-Service│────▶│   S3    │
│        │ POST│ /api/media/  │ PUT │         │
│        │file │   upload     │file │         │
└────────┘     └──────────────┘     └─────────┘
                      │
                      ▼ Save metadata
               ┌──────────────┐
               │  PostgreSQL  │
               └──────────────┘
```

### 10.2 Upload Direct via URL Présignée (Performant)
```
┌────────┐  1. Request URL   ┌──────────────┐
│ Client │──────────────────▶│ Media-Service│
│        │                   └──────┬───────┘
│        │◀─────────────────────────┘
│        │  2. Presigned URL + fileKey
│        │
│        │  3. PUT file directly
│        │──────────────────────────────────▶ ┌─────────┐
│        │                                    │   S3    │
│        │◀──────────────────────────────────┘└─────────┘
│        │  4. 200 OK
│        │
│        │  5. Confirm upload
│        │──────────────────▶┌──────────────┐
│        │                   │ Media-Service│
└────────┘                   └──────────────┘
```

---

## 11. Bucket S3 - Configuration Recommandée

### 11.1 Politique de Bucket (Production)
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "DenyPublicAccess",
            "Effect": "Deny",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::medina-heritage-media/*",
            "Condition": {
                "Bool": {
                    "aws:SecureTransport": "false"
                }
            }
        }
    ]
}
```

### 11.2 Règles de Cycle de Vie
- Fichiers `status=DELETED` : Suppression après 30 jours
- Versioning activé pour récupération accidentelle
- Transition vers S3 Glacier après 1 an (archivage)

### 11.3 CORS Configuration S3
```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["PUT", "POST", "GET"],
        "AllowedOrigins": ["http://localhost:3000", "https://medina-heritage.com"],
        "ExposeHeaders": ["ETag"],
        "MaxAgeSeconds": 3600
    }
]
```

---

## 12. Tests

### 12.1 Tests Unitaires
- `FileValidationServiceTest` : Validation des types MIME, tailles
- `MediaServiceTest` : Logique métier avec mocks S3
- `MediaMapperTest` : Conversion entity ↔ DTO

### 12.2 Tests d'Intégration
- `S3StorageServiceIntegrationTest` : Avec LocalStack ou S3 réel
- `MediaControllerIntegrationTest` : Tests API avec MockMvc

### 12.3 Configuration Test (H2 + Mock S3)
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
aws.s3.bucket-name=test-bucket
# Utiliser LocalStack ou mock S3Client
```

---

## 13. Checklist Implémentation

### Phase 1 : Core
- [ ] Entity `MediaFile` + Repository
- [ ] Enums (`MediaType`, `MediaStatus`, `EntityType`)
- [ ] DTOs (Request/Response)
- [ ] `MediaMapper`
- [ ] Exceptions + GlobalExceptionHandler

### Phase 2 : S3
- [ ] `S3Config` avec SDK AWS v2
- [ ] `S3StorageService` (upload, download URL, delete)
- [ ] `FileValidationService`

### Phase 3 : API
- [ ] `MediaService` (logique métier)
- [ ] `MediaController` (endpoints REST)
- [ ] Tests unitaires

### Phase 4 : Finalisation
- [ ] Tests d'intégration
- [ ] Documentation OpenAPI/Swagger
- [ ] Configuration CORS
- [ ] Logs et monitoring

---

## 14. Diagramme de Séquence - Upload

```
┌──────┐          ┌────────────────┐          ┌──────────────┐          ┌─────┐
│Client│          │MediaController │          │ MediaService │          │ S3  │
└──┬───┘          └───────┬────────┘          └──────┬───────┘          └──┬──┘
   │  POST /upload        │                          │                     │
   │  + MultipartFile     │                          │                     │
   │─────────────────────▶│                          │                     │
   │                      │  uploadFile(file, req)   │                     │
   │                      │─────────────────────────▶│                     │
   │                      │                          │  validateFile()     │
   │                      │                          │─────────┐           │
   │                      │                          │◀────────┘           │
   │                      │                          │  generateFileKey()  │
   │                      │                          │─────────┐           │
   │                      │                          │◀────────┘           │
   │                      │                          │                     │
   │                      │                          │   PutObject         │
   │                      │                          │────────────────────▶│
   │                      │                          │◀────────────────────│
   │                      │                          │   200 OK            │
   │                      │                          │                     │
   │                      │                          │  save(MediaFile)    │
   │                      │                          │─────────┐           │
   │                      │                          │◀────────┘  DB       │
   │                      │◀─────────────────────────│                     │
   │◀─────────────────────│  MediaResponse           │                     │
   │  ApiResponse         │                          │                     │
└──┴───┘          └───────┴────────┘          └──────┴───────┘          └──┴──┘
```

---

**Document rédigé le** : 5 Décembre 2025  
**Version** : 1.0 MVP  
**Auteur** : Conception Medina Heritage Platform
