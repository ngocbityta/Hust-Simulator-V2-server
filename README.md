# HUST Simulator V2 - Backend System

This repository contains the core backend services for the HUST Simulator V2.

### Quick Start
From this directory (`server`), run:

```bash
docker-compose up --build
```

### Published Ports

| Service | Port | Protocol | Description |
|---------|------|----------|-------------|
| **Context Service** | `8080` | HTTP | Domain APIs & Swagger UI |
| **State Dissemination** | `3002` | HTTP/WS | WebSocket Gateway & Monitoring |
| **PostgreSQL** | `5432` | TCP | Main database (PostGIS) |
| **Redis** | `6379` | TCP | State storage & Pub/Sub |
| **RabbitMQ** | `5672` | TCP | Message broker |
| **RabbitMQ Management**| `15672`| HTTP | Admin UI |

---

## API Documentation

- **Context API Reference**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **State Dissemination Monitoring**: [http://localhost:3002/api](http://localhost:3002/api)
