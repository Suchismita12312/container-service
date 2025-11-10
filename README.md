# Booking Service

A **Spring Boot WebFlux** microservice (Java 17 + Gradle) for checking container availability and creating bookings, showcasing **Reactive Programming**, **Resilience4j Circuit Breaker/Retry**, and **MongoDB**.

---

## 🧰 Tech Stack
| Layer | Tech |
|-------|------|
| Language | Java 17 |
| Framework | Spring Boot 2.7 (WebFlux) |
| Build | Gradle 7 |
| Resilience | Resilience4j |
| Data | MongoDB |
| Documentation | springdoc-openapi (Swagger UI) |
| Container | Docker / Docker Compose |

---

## ⚙️ Prerequisites
- Java 17+
- Gradle 7+
- Docker & Docker Compose

---

## 🏗️ Build and Run Locally
```bash
git clone https://github.com/<your-org>/container-service.git
cd container-service
./gradlew clean bootJar
docker run -d --name mongo -p 27017:27017 mongo:6.0
java -jar build/libs/container-service-0.0.1-SNAPSHOT.jar
```

## Endpoints
| URL | Purpose |
|-------|------|
| http://localhost:8080/api/bookings/check-availability | Check container availability |
| http://localhost:8080/api/bookings | Create booking |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/v3/api-docs | Live OpenAPI JSON |
| http://localhost:8080/actuator/health | Healthcheck |

---

## 🐳 Run with Docker
```bash
docker build -t container-service:latest .
docker run --rm -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI="mongodb://host.docker.internal:27017/demo" \
  container-service:latest
```

---

## 🧩 Run Full Stack (Docker Compose)
```bash
docker compose up -d
docker compose logs -f app
```
Access **Swagger UI** at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**Stop & clean:**
```bash
docker compose down -v
```

---

## 📄 OpenAPI / Swagger
**Auto-generated (recommended):**
- `/v3/api-docs`
- `/swagger-ui.html`

**Static spec (optional):**
`src/main/resources/static/openapi/openapi.yaml`
```yaml
springdoc:
  swagger-ui:
    url: /openapi/openapi.yaml
```

---

## 🧠 Reactive & Resilience Highlights
- End-to-end **non-blocking** WebFlux
- **Resilience4j** Circuit Breaker + Retry with exponential back-off & jitter
- Graceful handling of empty responses (returns `{ "available": false }`)
- Unified RFC 7807 error responses

---

## 🧹 Cleanup
```bash
docker compose down -v
docker image rm demo-booking:latest
docker volume prune
```

---

## 🧾 License
MIT — free to use and extend.
