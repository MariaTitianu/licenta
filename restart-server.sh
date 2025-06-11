#!/bin/bash
# Server restart script for licenta project

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
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

# If -v flag provided, remove only PostgreSQL volume
if [[ "$RESET_DB" == true ]]; then
    echo -e "${YELLOW}Removing PostgreSQL data volume...${NC}"
    docker volume rm licenta_postgres_data 2>/dev/null
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
    echo "Services available at:"
    echo "  - PostgreSQL: localhost:5433"
    echo "  - pgAdmin: http://localhost:5050"
    echo "    Email: admin@licenta.com"
    echo "    Password: admin"
    echo
    echo "Database users:"
    echo "  - postgres / postgres (superuser)"
    echo "  - warden_admin_user / warden_admin_pass (table protection admin)"
    echo "  - regular_user / regular_user_pass (regular user)"
    
    if [[ "$RESET_DB" == true ]]; then
        echo
        echo -e "${YELLOW}Note: PostgreSQL database was recreated from scratch (initialization script was run)${NC}"
        echo -e "${GREEN}pgAdmin settings and saved servers were preserved${NC}"
    fi
else
    echo -e "${RED}Failed to start containers${NC}"
    exit 1
fi