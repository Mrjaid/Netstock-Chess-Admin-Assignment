# ♟️ Chess Admin App

A Vaadin + Spring Boot application for managing chess club members, matches and ranking logic.

This repository contains a Spring Boot backend and Vaadin Flow frontend. The app uses an H2 file-backed database by default (so data persists between restarts) and can be run locally (IDE or CLI) or in a Docker container using docker-compose.

---

## 🚀 This README includes:
- Prerequisites
- Project structure (high level)
- How to run in IntelliJ (IDE)
- How to run from the command line (Maven)
- How to run the tests (unit + integration)
- Docker & docker-compose instructions (build/run)
- How H2 persistence is configured and how to open the H2 console
- Troubleshooting and tips

---

## 🧩 Prerequisites
- Java 17 or later (JDK must match project's `<java.version>` - currently 17)
- Maven 3.8+
- (Optional) Docker & Docker Compose (for containerized runs)
- Recommended: IntelliJ IDEA (Community or Ultimate) for best Vaadin/Java experience

> Note: The project `pom.xml` currently sets `<java.version>17`.

---

## 📦 Project structure (high level)
- `src/main/java` - Java sources (entities, services, repositories, Vaadin views)
- `src/main/resources` - configuration (application.properties)
- `src/test/java` - unit & integration tests
- `target/` - build output (jar, compiled classes)
- `Dockerfile`, `docker-compose.yml` - containerization

---

## ▶️ Run locally (IDE - IntelliJ)
1. Open IntelliJ and choose "Open" and select this repository folder.
2. Make sure the project SDK is set to Java 17 (File → Project Structure → Project SDK).
3. Build the project (Build → Build Project) or run Maven goal `package`.
4. Run the main application class `com.netstock.chessadmin.ChessAdminApplication` (Run → Run 'ChessAdminApplication').

The application starts on port 8080 by default. Open http://localhost:8080 in your browser.

To use the H2 console (for debugging): http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/h2/chess_admin`
- User: `sa`
- Password: (leave empty)

---

## ▶️ Run from CLI (Maven)
From the repository root:

1) Build

```bash
mvn clean package
```

2) Run (via Spring Boot plugin)

```bash
mvn spring-boot:run
```

or run the packaged jar

```bash
java -jar target/chess_admin-0.0.1-SNAPSHOT.jar
```

Open http://localhost:8080 and the H2 console at http://localhost:8080/h2-console.

Notes:
- The app is configured to use H2 file mode at `./data/h2` (see `src/main/resources/application.properties`) so DB files will be created relative to the working directory. Keep that folder across restarts to preserve data.

---

## ✅ Running tests
- Run all tests (unit + integration):

```bash
mvn -Djacoco.skip=false test
```

- If you want to run the full verify lifecycle (integration tests may be run depending on plugin/config):

```bash
mvn -Djacoco.skip=false verify
```

Notes about tests in this repo:
- There is a mixture of unit and integration tests under `src/test/java`. If you prefer faster iteration, you can filter tests by package or name with Maven's `-Dtest=` option.

---

## 🐳 Run in Docker (containerized)
Two primary options: use Docker directly or use `docker-compose`. The provided `docker-compose.yml` will mount `./data/h2` so the H2 database files are persisted on the host.

### Build and run with docker-compose
From repository root:

```bash
# make sure the host data folder exists
mkdir -p ./data/h2
chmod -R 755 ./data

# build and start
docker-compose up --build -d

# follow logs
docker-compose logs -f

# stop and remove containers
docker-compose down
```

The app will be available at http://localhost:8080.

### Build and run with Docker directly

```bash
# build image
docker build -t chess_admin:local .

# run container, mount host data folder for H2 persistence
docker run --rm -p 8080:8080 -v "$(pwd)/data/h2:/app/data/h2" --name chess_admin_app chess_admin:local
```

### Docker notes
- `application.properties` points H2 URL to `jdbc:h2:file:./data/h2/chess_admin`. In the container `./data/h2` maps to `/app/data/h2`. The volume mapping ensures the same path on host is used by the app in the container.
- If you change the working directory or image entrypoint you may need to adjust the path.

---

## 🔐 Environment / Profiles
- The app uses the default Spring profile unless you provide `SPRING_PROFILES_ACTIVE` in the environment. The `docker-compose.yml` sets `SPRING_PROFILES_ACTIVE=default` by default.

---

## ℹ️ H2 Persistence
- The application uses H2 in file mode so data is persisted under `data/h2` in the project root (or the mounted folder when running in Docker).
- On first run, H2 files will be created: `chess_admin.mv.db`, `chess_admin.trace.db`, etc.
- Use the H2 console at `/h2-console` to inspect tables and data.

---

## 🛠 Troubleshooting
- If the application fails to start:
  - Check logs (in IntelliJ console or `docker-compose logs`)
  - Confirm Java version matches project's requirement
  - Confirm `./data/h2` is writable (when using docker volume)
- If tests fail when running `mvn verify`, run only unit tests to isolate issues:

```bash
mvn -Djacoco.skip=true -Dtest="**/*UnitTest" test
```
