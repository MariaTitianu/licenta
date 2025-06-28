# Complete application startup script for licenta project
# Starts all services: PostgreSQL, Spring Boot backends, and React frontend

param(
    [switch]$ResetDb
)

# Color functions
function Write-Green { Write-Host $args -ForegroundColor Green }
function Write-Yellow { Write-Host $args -ForegroundColor Yellow }
function Write-Red { Write-Host $args -ForegroundColor Red }
function Write-Blue { Write-Host $args -ForegroundColor Blue }

# Check if --reset-db flag is provided
if ($ResetDb) {
    Write-Yellow "Database reset requested - PostgreSQL data will be wiped!"
    Write-Green "pgAdmin settings will be preserved"
}

# Check if containers are running
$runningContainers = docker compose ps --quiet 2>$null
if ($runningContainers) {
    Write-Yellow "Containers are running. Stopping..."
    docker compose down
    if ($LASTEXITCODE -eq 0) {
        Write-Green "Containers stopped successfully"
    } else {
        Write-Red "Failed to stop containers"
        exit 1
    }
} else {
    Write-Yellow "No running containers found"
}

# If --reset-db flag provided, remove PostgreSQL volume
if ($ResetDb) {
    Write-Yellow "Removing PostgreSQL data volume..."
    # Add a small delay to ensure volume is released
    Start-Sleep -Seconds 1
    # Force remove the volume
    docker volume rm -f licenta_postgres_data 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Green "PostgreSQL data volume removed"
    } else {
        Write-Yellow "PostgreSQL data volume not found or already removed"
    }
}

# Rebuild and start containers
Write-Yellow "Building and starting containers..."
docker compose up -d --build

if ($LASTEXITCODE -eq 0) {
    Write-Green "Containers started successfully!"
    Write-Host ""
    Write-Blue "=== Services Overview ==="
    Write-Host ""
    Write-Green "Frontend:"
    Write-Host "  - React App: http://localhost:3000"
    Write-Host ""
    Write-Green "Backend Services:"
    Write-Host "  - JPA + Warden Admin:    http://localhost:8081"
    Write-Host "  - JPA + Warden User:     http://localhost:8082"
    Write-Host "  - JDBC + Warden Admin:   http://localhost:8083"
    Write-Host "  - JDBC + Warden User:    http://localhost:8084"
    Write-Host "  - JPA + Vanilla Admin:   http://localhost:8085"
    Write-Host "  - JDBC + Vanilla Admin:  http://localhost:8086"
    Write-Host ""
    Write-Green "Database Services:"
    Write-Host "  - PostgreSQL: localhost:5433"
    Write-Host "  - pgAdmin: http://localhost:5050"
    Write-Host "    Email: admin@licenta.com"
    Write-Host "    Password: admin"
    Write-Host ""
    Write-Green "Database Users:"
    Write-Host "  - postgres / postgres (superuser)"
    Write-Host "  - warden_admin_user / warden_admin_pass (table protection admin)"
    Write-Host "  - regular_user / regular_user_pass (regular user)"
    
    if ($ResetDb) {
        Write-Host ""
        Write-Yellow "Note: PostgreSQL database was recreated from scratch (initialization script was run)"
        Write-Green "pgAdmin settings and saved servers were preserved"
    }
    
    Write-Host ""
    Write-Blue "To stop all services, run: docker compose down"
} else {
    Write-Red "Failed to start containers"
    exit 1
}