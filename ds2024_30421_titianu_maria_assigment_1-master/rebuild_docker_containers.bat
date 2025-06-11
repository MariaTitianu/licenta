@echo off
docker compose down --rmi local
docker volume prune -a -f
docker compose up --build -d
