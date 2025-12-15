# run-integration.ps1
# 1. Charge TOUTES les variables du fichier .env
Get-Content .env | ForEach-Object {
    if ($_ -match "^\s*[^#].*=") {
        $k, $v = $_.Split('=', 2)
        [Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), "Process")
    }
}

# 2. Affiche le port pour vérifier (devrait être 8086)
Write-Host "Lancement de Integration Service sur le port $env:INTEGRATION_SALESFORCE_SERVICE_PORT..."

# 3. Lance le service
.\mvnw -pl integration-salesforce-service spring-boot:run -DskipTests