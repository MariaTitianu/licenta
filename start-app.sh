#!/bin/bash
# Complete application startup script for licenta project
# Starts all services: PostgreSQL, Spring Boot backends, and React frontend

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if --reset-db flag is provided
RESET_DB=false
if [[ "$1" == "--reset-db" ]]; then
    RESET_DB=true
    echo -e "${YELLOW}Database reset requested - PostgreSQL data will be wiped!${NC}"
    echo -e "${GREEN}pgAdmin settings will be preserved${NC}"
fi

# Check if containers are running
if docker compose ps --quiet 2>/dev/null | grep -q .; then
    echo -e "${YELLOW}Containers are running. Stopping...${NC}"
    docker compose down
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}Containers stopped successfully${NC}"
    else
        echo -e "${RED}Failed to stop containers${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}No running containers found${NC}"
fi

# If --reset-db flag provided, remove PostgreSQL volume
if [[ "$RESET_DB" == true ]]; then
    echo -e "${YELLOW}Removing PostgreSQL data volume...${NC}"
    # Add a small delay to ensure volume is released
    sleep 1
    # Force remove the volume
    docker volume rm -f licenta_postgres_data 2>/dev/null
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}PostgreSQL data volume removed${NC}"
    else
        echo -e "${YELLOW}PostgreSQL data volume not found or already removed${NC}"
    fi
fi

# Rebuild and start containers
echo -e "${YELLOW}Building and starting containers...${NC}"
docker compose up -d --build

if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}Containers started successfully!${NC}"
    echo
    echo -e "${BLUE}=== Services Overview ===${NC}"
    echo
    echo -e "${GREEN}Frontend:${NC}"
    echo "  - React App: http://localhost:3000"
    echo
    echo -e "${GREEN}Backend Services:${NC}"
    echo "  - JPA + Warden Admin:    http://localhost:8081"
    echo "  - JPA + Warden User:     http://localhost:8082"
    echo "  - JDBC + Warden Admin:   http://localhost:8083"
    echo "  - JDBC + Warden User:    http://localhost:8084"
    echo "  - JPA + Vanilla Admin:   http://localhost:8085"
    echo "  - JDBC + Vanilla Admin:  http://localhost:8086"
    echo
    echo -e "${GREEN}Database Services:${NC}"
    echo "  - PostgreSQL: localhost:5433"
    echo "  - pgAdmin: http://localhost:5050"
    echo "    Email: admin@licenta.com"
    echo "    Password: admin"
    echo
    echo -e "${GREEN}Database Users:${NC}"
    echo "  - postgres / postgres (superuser)"
    echo "  - warden_admin_user / warden_admin_pass (table protection admin)"
    echo "  - regular_user / regular_user_pass (regular user)"
    
    if [[ "$RESET_DB" == true ]]; then
        echo
        echo -e "${YELLOW}Note: PostgreSQL database was recreated from scratch (initialization script was run)${NC}"
        echo -e "${GREEN}pgAdmin settings and saved servers were preserved${NC}"
    fi
    
    echo
    echo -e "${BLUE}To stop all services, run: docker compose down${NC}"
else
    echo -e "${RED}Failed to start containers${NC}"
    exit 1
fi