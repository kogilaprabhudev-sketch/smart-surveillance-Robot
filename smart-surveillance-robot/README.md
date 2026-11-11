# Smart Public Surveillance Robot – Spring Boot Backend

Java 17 + Spring Boot 3 + MySQL backend for the *Smart Public Surveillance Robot* project report.
The Raspberry Pi keeps doing the camera / YOLO / motor work; this server **stores everything in SQL,
sends Telegram alerts, answers Telegram bot commands, and lets operators drive the robot from a web dashboard.**

```
Raspberry Pi (Python) ──HTTP + X-API-KEY──►  Spring Boot  ──►  MySQL
   sensors, YOLO detections                    │  ├─ Telegram alerts + bot (/status /last /time /date)
   polls motor commands  ◄────────────────────┘  └─ Dashboard + REST API (HTTP Basic, ADMIN/OPERATOR)
```

## What from the report is implemented

| Report feature | Where |
|---|---|
| Alert when a valid YOLO detection (confidence ≥ threshold **and** target class) occurs, with GPS location | `DetectionService` |
| Robot stops on a valid threat | queues a `STOP` command |
| Ultrasonic "Low Distance" alert (< 5 cm) | `SensorService` |
| IR / sound sensor data, event logging ("robot must log detection events") | tables `sensor_readings`, `detection_events` |
| Alerts sent in a separate thread | `AlertDispatcher` (`@Async`) |
| Telegram bot commands `/status /time /date` (+ `/last`, `/help`) | `TelegramBotService` |
| Motor commands forward / reverse / left / right / stop | `movement_commands` + dashboard pad |
| Secure handling of communication data | API key for the robot, BCrypt + roles for people |

## Database tables (`sql/schema.sql`)

`robots`, `sensor_readings`, `detection_events`, `movement_commands`, `alerts`, `app_users`

## Run it

**Option A – quick try, no MySQL (in-memory H2):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option B – MySQL:**
1. Install MySQL 8, then (optional) run `sql/schema.sql` — the app also creates the tables itself.
2. Set environment variables (examples for Linux/macOS; use `set` on Windows):
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export ROBOT_API_KEY=a-long-random-secret
export ADMIN_PASSWORD='choose-a-strong-one'
export OPERATOR_PASSWORD='choose-another'
# optional Telegram
export TELEGRAM_BOT_TOKEN=...      # from @BotFather
export TELEGRAM_CHAT_ID=...        # your chat id
mvn spring-boot:run
```
3. Open http://localhost:8080 and sign in (default `admin` / `Admin@123` – **change it**).
   A robot named `SPSR-01` is created automatically.

Run tests: `mvn test` (uses H2).

## Connect the Raspberry Pi

Copy `raspberry_pi/robot_client.py` to the Pi, `pip install requests`, then:
```bash
SERVER_URL=http://<server-ip>:8080 ROBOT_API_KEY=<same key> python3 robot_client.py
```
From your YOLO loop call `send_detection("person", 0.87, lat, lon)`.
Your existing `app.py` camera stream keeps running on port 8000; paste its URL into the dashboard's *Live camera* box.

## REST API

Robot endpoints – header `X-API-KEY: <ROBOT_API_KEY>`

| Method | URL | Body |
|---|---|---|
| POST | `/api/robot/{code}/heartbeat` | `{"latitude":17.54,"longitude":78.57}` (optional) |
| POST | `/api/robot/{code}/sensors` | `{"ultrasonicCm":3.2,"irTriggered":false,"soundTriggered":true}` |
| POST | `/api/robot/{code}/detections` | `{"objectClass":"person","confidence":0.87,"latitude":17.54,"longitude":78.57}` |
| GET | `/api/robot/{code}/commands/next` | 200 with a command, or 204 |

Operator endpoints – HTTP Basic (ADMIN or OPERATOR)

| Method | URL | Notes |
|---|---|---|
| GET | `/api/robots`, `/api/robots/{code}` | status, last seen, location |
| POST | `/api/robots` | **ADMIN**: `{"robotCode":"SPSR-02","name":"North gate"}` |
| POST | `/api/robots/{code}/commands` | `{"command":"FORWARD"}` (FORWARD/REVERSE/LEFT/RIGHT/STOP) |
| GET | `/api/robots/{code}/commands`, `/sensors`, `/sensors/latest`, `/detections` | paged: `?page=0&size=20` |
| GET | `/api/detections?robotCode=SPSR-01` | newest first |
| GET | `/api/alerts?acknowledged=false` | |
| POST | `/api/alerts/{id}/acknowledge` | |
| GET | `/api/stats`, `/api/me` | |

Try it:
```bash
curl -X POST localhost:8080/api/robot/SPSR-01/detections \
  -H "X-API-KEY: change-this-robot-key" -H "Content-Type: application/json" \
  -d '{"objectClass":"person","confidence":0.91,"latitude":17.5449,"longitude":78.5718}'

curl -u admin:Admin@123 localhost:8080/api/alerts
```

## Settings (`application.properties`)

`robot.confidence-threshold` (0.50), `robot.target-classes` (person), `robot.distance-alert-cm` (5),
`robot.alert-cooldown-seconds` (30 – stops Telegram spam), `robot.heartbeat-timeout-seconds` (60 → robot marked OFFLINE).

## Security notes

* **Revoke the Telegram bot token printed in your report's code** (BotFather → `/revoke`) – it is public now.
  This project reads the token from the `TELEGRAM_BOT_TOKEN` environment variable instead of hard-coding it.
* Change `ROBOT_API_KEY`, `ADMIN_PASSWORD`, `OPERATOR_PASSWORD` before real use, and put the server behind HTTPS.
* The Telegram bot only answers the chat in `TELEGRAM_CHAT_ID`.

## Project layout

```
src/main/java/com/surveillance/robot
  config/      properties, security chain, seed data
  controller/  REST endpoints
  service/     business rules, Telegram client + bot, async alert dispatch
  repository/  Spring Data JPA
  model/       JPA entities + enums     dto/  request/response records
  security/    API-key filter, DB users   exception/  error handling
src/main/resources/static/index.html   dashboard
sql/schema.sql, sql/sample_data.sql    MySQL scripts
raspberry_pi/robot_client.py           Pi-side client
```
