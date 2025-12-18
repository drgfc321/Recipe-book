# D&D Campaign Manager

A web application for managing Dungeons & Dragons campaigns, characters, and sessions.

## Tech Stack

| Layer      | Technology              | Port  |
|------------|-------------------------|-------|
| Frontend   | Vaadin 24 + Spring Boot | 8081  |
| Backend    | Quarkus 3               | 8080  |
| Database   | PostgreSQL 16           | 5432  |
| DB Admin   | Adminer                 | 8082  |

## Prerequisites

- Docker Desktop
- Java 21+
- Maven 3.9+

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd dnd-campaign-manager
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env if you want to change default values
   ```

3. **Start the database**
   ```bash
   docker-compose up -d
   ```

4. **Verify containers are running**
   ```bash
   docker ps
   ```

## Access Points

| Service    | URL                          | Credentials                    |
|------------|------------------------------|--------------------------------|
| Adminer    | http://localhost:8082        | System: PostgreSQL             |
|            |                              | Server: postgres               |
|            |                              | User: dnd_user                 |
|            |                              | Password: dnd_secret           |
|            |                              | Database: dnd_campaign         |
| PostgreSQL | localhost:5432               | (same credentials as above)    |
| Backend    | http://localhost:8080        | (not started yet)              |
| Frontend   | http://localhost:8081        | (not started yet)              |

## Docker Commands

```bash
# Start containers
docker-compose up -d

# Stop containers
docker-compose down

# View logs
docker-compose logs -f

# Reset database (delete all data)
docker-compose down -v
docker-compose up -d
```

## Project Structure

```
dnd-campaign-manager/
├── docker-compose.yml    # PostgreSQL + Adminer setup
├── .env                  # Environment variables (not in git)
├── .env.example          # Template for .env
├── .gitignore            # Git ignore patterns
├── backend/              # Quarkus REST API (TODO)
└── frontend/             # Vaadin UI (TODO)
```
