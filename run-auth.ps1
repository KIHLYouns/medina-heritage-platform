# run-auth.ps1
# Charge les variables
Get-Content .env | ForEach-Object {
    if ($_ -match "^\s*[^#].*=") {
        $k, $v = $_.Split('=', 2)
        [Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), "Process")
    }
}
# Lance le service Auth
Write-Host "Lancement de User Auth Service sur le port $env:USER_AUTH_SERVICE_PORT..."
.\mvnw -pl user-auth-service spring-boot:run -DskipTests