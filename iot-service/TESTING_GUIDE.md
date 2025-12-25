# Guide de test - IoT Service avec Spring Cloud Stream

Ce guide explique comment tester l'intégration Spring Cloud Stream pour le service IoT.

## Prérequis

1. **RabbitMQ en cours d'exécution**
   ```bash
   # Vérifier que RabbitMQ est démarré
   rabbitmqctl status
   # Interface web: http://localhost:15672 (guest/guest)
   ```

2. **Services démarrés**
   - iot-service (port 8083)
   - integration-salesforce-service (port 8086)
   - Base de données PostgreSQL

3. **Données de test dans la base de données**
   - Au moins un device (VIBRATION ou HUMIDITY)
   - Au moins une règle de risque (risk_rules)

## Architecture de test

```
Node-RED (ou test manuel) 
  → RabbitMQ queue "iot.measurements"
  → iot-service consumer
  → Persistence dans measurements
  → Évaluation des seuils
  → Si dépassement: publication "iot.risk.alert"
  → integration-salesforce-service consumer
  → Création Case Salesforce
```

## Étape 1: Préparer les données de test

### 1.1 Insérer un device dans la base de données

```sql
INSERT INTO devices (id, serial_number, type, building_id, sf_asset_id, status)
VALUES (
    gen_random_uuid(),
    'VIB-001',
    'VIBRATION',
    gen_random_uuid(), -- ou un UUID réel de building
    '001XX000003XYZAA', -- ID Salesforce Asset (optionnel)
    'ONLINE'
);
```

### 1.2 Insérer une règle de risque

```sql
INSERT INTO risk_rules (metric_type, threshold_min, threshold_max, severity_level, description)
VALUES (
    'VIBRATION',
    0.0,      -- seuil minimum
    50.0,     -- seuil maximum (valeur critique)
    'CRITICAL',
    'Vibration excessive détectée - risque structurel'
);

-- Pour HUMIDITY
INSERT INTO risk_rules (metric_type, threshold_min, threshold_max, severity_level, description)
VALUES (
    'HUMIDITY',
    30.0,     -- seuil minimum
    80.0,     -- seuil maximum
    'WARNING',
    'Humidité hors norme'
);
```

## Étape 2: Tester le consumer Node-RED (réception de données)

### Option A: Utiliser RabbitMQ Management UI

1. Ouvrir http://localhost:15672
2. Aller dans l'onglet "Exchanges"
3. Chercher ou créer l'exchange "iot.measurements"
4. Cliquer sur "Publish message"
5. Envoyer ce JSON dans le payload:

```json
{
  "serial_number": "VIB-001",
  "device_id": null,
  "value": 75.5,
  "unit": "Hz",
  "metric_type": "VIBRATION",
  "timestamp": null
}
```

**Message qui DÉPASSE le seuil (devrait déclencher une alerte):**
```json
{
  "serial_number": "VIB-001",
  "device_id": null,
  "value": 75.5,
  "unit": "Hz",
  "metric_type": "VIBRATION",
  "timestamp": null
}
```

**Message DANS les seuils (pas d'alerte):**
```json
{
  "serial_number": "VIB-001",
  "device_id": null,
  "value": 30.0,
  "unit": "Hz",
  "metric_type": "VIBRATION",
  "timestamp": null
}
```

### Option B: Utiliser la ligne de commande RabbitMQ

```bash
# Publier un message via rabbitmqadmin (si installé)
rabbitmqadmin publish exchange=iot.measurements routing_key="" payload='{"serial_number":"VIB-001","value":75.5,"unit":"Hz","metric_type":"VIBRATION"}'
```

### Option C: Utiliser un script Python de test

Créez un fichier `test_send_measurement.py`:

```python
import pika
import json

connection = pika.BlockingConnection(
    pika.ConnectionParameters('localhost')
)
channel = connection.channel()

# Déclarer l'exchange (Spring Cloud Stream le créera automatiquement)
channel.exchange_declare(exchange='iot.measurements', exchange_type='topic')

message = {
    "serial_number": "VIB-001",
    "device_id": None,
    "value": 75.5,
    "unit": "Hz",
    "metric_type": "VIBRATION",
    "timestamp": None
}

channel.basic_publish(
    exchange='iot.measurements',
    routing_key='',
    body=json.dumps(message),
    properties=pika.BasicProperties(
        content_type='application/json'
    )
)

print(f"Message envoyé: {message}")
connection.close()
```

```bash
pip install pika
python test_send_measurement.py
```

### Option D: Utiliser cURL via HTTP (alternative - garde le endpoint REST)

```bash
curl -X POST http://localhost:8083/api/iot/nodered/measurements \
  -H "Content-Type: application/json" \
  -d '{
    "serial_number": "VIB-001",
    "value": 75.5,
    "unit": "Hz",
    "metric_type": "VIBRATION"
  }'
```

## Étape 3: Vérifier les logs

### Vérifier que le message a été reçu par iot-service

```bash
# Dans les logs de iot-service, vous devriez voir:
# "Received measurement from Node-RED via RabbitMQ: serialNumber=VIB-001..."
# "Successfully processed measurement from Node-RED: measurementId=..."
```

### Vérifier l'évaluation des risques

```bash
# Si la valeur dépasse le seuil, vous devriez voir:
# "Risk rule triggered for device VIB-001 metricType=VIBRATION value=75.5 direction=ABOVE_MAX severity=CRITICAL -> published via Spring Cloud Stream"
```

### Vérifier que l'événement a été publié

```bash
# Dans RabbitMQ Management UI, vérifier l'exchange "iot.risk.alert"
# Il devrait contenir un message avec l'événement RiskAlertEvent
```

## Étape 4: Vérifier la consommation par integration-salesforce-service

### Vérifier les logs de integration-salesforce-service

```bash
# Vous devriez voir:
# "Received RiskAlertEvent: deviceId=..., metricType=VIBRATION, severity=CRITICAL..."
# "Risk Alert Case created in Salesforce: 500XX000003..."
```

### Vérifier dans la base de données

```sql
-- Vérifier que la mesure a été sauvegardée
SELECT * FROM measurements ORDER BY time DESC LIMIT 5;

-- Vérifier les devices mis à jour
SELECT * FROM devices WHERE serial_number = 'VIB-001';
```

## Étape 5: Tests avec différents scénarios

### Test 1: Valeur normale (pas d'alerte)
```json
{
  "serial_number": "VIB-001",
  "value": 25.0,
  "unit": "Hz",
  "metric_type": "VIBRATION"
}
```
**Résultat attendu:** Mesure sauvegardée, pas d'alerte publiée

### Test 2: Valeur au-dessus du seuil max
```json
{
  "serial_number": "VIB-001",
  "value": 100.0,
  "unit": "Hz",
  "metric_type": "VIBRATION"
}
```
**Résultat attendu:** 
- Mesure sauvegardée
- Alerte CRITICAL publiée
- Case créé dans Salesforce

### Test 3: Valeur en-dessous du seuil min (si configuré)
```json
{
  "serial_number": "VIB-001",
  "value": -10.0,
  "unit": "Hz",
  "metric_type": "VIBRATION"
}
```
**Résultat attendu:** Alerte BELOW_MIN publiée

### Test 4: Device inexistant
```json
{
  "serial_number": "UNKNOWN-999",
  "value": 50.0,
  "unit": "Hz",
  "metric_type": "VIBRATION"
}
```
**Résultat attendu:** Erreur dans les logs, message rejeté

## Étape 6: Vérifier dans RabbitMQ Management

1. **Vérifier les exchanges créés:**
   - `iot.measurements` (input)
   - `iot.risk.alert` (output)

2. **Vérifier les queues créées:**
   - `iot.measurements.iot-service` (consumer group)
   - `iot.risk.alert.integration-salesforce-service` (consumer group)

3. **Vester les messages:**
   - Aller dans l'exchange `iot.risk.alert`
   - Cliquer sur "Get message(s)" pour voir le contenu

## Dépannage

### Le message n'est pas consommé

1. Vérifier que RabbitMQ est accessible:
   ```bash
   rabbitmqctl list_exchanges
   rabbitmqctl list_queues
   ```

2. Vérifier les logs d'erreur de iot-service:
   ```bash
   # Chercher des erreurs de connexion RabbitMQ
   grep -i "rabbit" logs/iot-service.log
   ```

3. Vérifier la configuration Spring Cloud Stream:
   - Vérifier `application.properties` que les bindings sont corrects
   - Vérifier que `spring.cloud.function.definition` inclut le consumer

### L'événement n'est pas publié

1. Vérifier que le seuil est bien dépassé:
   ```sql
   SELECT * FROM risk_rules WHERE metric_type = 'VIBRATION';
   SELECT * FROM measurements ORDER BY time DESC LIMIT 1;
   ```

2. Vérifier les logs de RiskEvaluationService:
   ```bash
   grep -i "risk rule triggered" logs/iot-service.log
   ```

### Le consumer Salesforce ne reçoit pas l'événement

1. Vérifier que l'événement est bien publié dans RabbitMQ
2. Vérifier que `spring.cloud.function.definition` inclut `riskAlertConsumer`
3. Vérifier les logs d'erreur de integration-salesforce-service

## Tests automatisés (optionnel)

Pour des tests plus robustes, vous pouvez créer des tests unitaires et d'intégration avec `spring-cloud-stream-test-binder`.

