# BlueZone

A modular, hexagonal-architecture reference implementation of a **Blue Zone
street-parking system** — the pay-and-display parking scheme common in Spanish and
French city centers. Drivers purchase a time-limited parking ticket for a rate zone
(e.g. `BLUE_ZONE`, `GREEN_ZONE`, `ORANGE_ZONE`); enforcement checks whether a parked
car currently holds a valid ticket.

BlueZone is built as a teaching-grade demonstration of **Ports & Adapters
(Hexagonal Architecture)** using native **Java Platform Modules (JPMS)**, where
every port and every adapter is its own Maven/JPMS module, and adapters are wired
at runtime via `ServiceLoader`.

---

## Features

- Purchase a parking ticket for a car plate and rate zone, paying by card.
- Look up an existing ticket by code.
- List all available rate zones and their per-hour price.
- Detect whether a parked car is illegally parked (no valid ticket covering the
  current time).
- Pluggable persistence, payment, and rate-provider adapters — swap real
  infrastructure in without touching the domain.
- Two independent UIs over the same core: a Spring Boot + Thymeleaf web app, and
  Cucumber/TestNG acceptance-test drivers.

## Business problem solved

Municipal blue-zone parking needs three independently-evolving concerns kept
decoupled: **pricing** (rates change per zone/city), **payment** (multiple payment
providers over time), and **ticket storage** (in-memory today, a real database
tomorrow). BlueZone's hexagonal design lets each of these evolve or be replaced
without the core parking/checking logic ever changing.

## Architecture overview

```
                    ┌─────────────────────────┐
   Driving side      │        bluezone-hexagon  │      Driven side
  ────────────►      │  ForParkingCars          │      ◄────────────
                      │  ForCheckingCars         │
  Web UI (Spring)     │  ForConfiguringApp       │      ForObtainingRates
  Cucumber tests  ───►│                          │◄──── ForStoringTickets
  TestNG tests         │  CarParker / CarChecker  │      ForPaying
                      │  RateCalculator          │
                      └─────────────────────────┘
```

- **`bluezone-hexagon`** — the core. Defines *driving ports* (what the app offers:
  `ForParkingCars`, `ForCheckingCars`, `ForConfiguringApp`) and *driven ports* (what
  the app needs: `ForObtainingRates`, `ForStoringTickets`, `ForPaying`), plus the
  domain logic (`CarParker`, `CarChecker`, `RateCalculator`) behind them.
- **Driven adapters** (implement what the core needs): a stub rate provider, a fake
  in-memory ticket store, and a spy payment service — each a self-contained module
  registered as a JPMS service (`provides ForX with YAdapter`).
- **Driving adapters/drivers** (call into the core): a Spring Boot + Thymeleaf web
  UI, and Cucumber/TestNG acceptance-test drivers that run Gherkin `.feature` files
  against the same core.
- **`bluezone-startup`** — the composition root. Reads
  `scripts/ports-adapters.properties` to decide which adapter implements each port,
  resolves them via `ServiceLoader`, wires the application, and runs the selected
  driver(s).

This means the same domain logic backs both the web app and the automated test
suite, and swapping the fake ticket store for a real database only requires writing
one new adapter module — no change to `bluezone-hexagon`.

## Technologies used

| Concern              | Technology                                   |
|-----------------------|-----------------------------------------------|
| Language / platform    | Java 11, Java Platform Modules (JPMS)          |
| Build                  | Maven (multi-module reactor)                  |
| Web framework           | Spring Boot 2.7, Spring MVC, Thymeleaf         |
| Styling                | Bootstrap 5 (webjar)                          |
| Boilerplate reduction   | Lombok                                        |
| Acceptance testing      | Cucumber 6 (Gherkin) with PicoContainer DI     |
| Unit/scenario testing   | TestNG, Hamcrest                              |
| Adapter wiring          | Custom `lib-portsadapters` (`ServiceLoader` + `@Adapter` annotation) |

## Project structure

```
bluezone/
├── pom.xml                                    # Maven reactor / aggregator
├── scripts/
│   ├── build.sh
│   ├── run_bluezone.sh
│   └── ports-adapters.properties              # which adapter serves which port
└── src/
    ├── bluezone-parent/                       # shared dependency management
    ├── bluezone-hexagon/                      # ports + domain logic
    ├── bluezone-adapter-forobtainingrates-stub/
    ├── bluezone-adapter-forstoringtickets-fake/
    ├── bluezone-adapter-forpaying-spy/
    ├── bluezone-adapter-forparkingcars-webui/ # Spring Boot + Thymeleaf UI
    ├── bluezone-driver-forparkingcars-test/   # Cucumber acceptance tests
    ├── bluezone-driver-forcheckingcars-test/  # TestNG acceptance tests
    └── bluezone-startup/                      # composition root / entry point
```

## Prerequisites

- JDK 11+
- Maven 3.6+

## Installation

```bash
git clone <repository-url>
cd bluezone
mvn -f pom.xml clean install
```

This builds every module in dependency order and installs them to your local
Maven repository (and a project-local repo under `target/localrepo`, per the
aggregator POM's `distributionManagement`).

## Configuration

Adapter selection is driven entirely by `scripts/ports-adapters.properties`. Each
line maps a **port** (by simple interface name) to the **name** of the adapter that
should implement it (matching the `@Adapter(name = "...")` annotation on the
adapter class):

```properties
ForObtainingRates=test-double
ForStoringTickets=test-double
ForPaying=test-double
ForParkingCars=web-ui
ForCheckingCars=test-cases
```

Change these values to point at different adapter implementations as they're added
(e.g. a future JDBC ticket store) without recompiling the core.

## Running the application

**Via the startup module (uses `ports-adapters.properties`):**

```bash
./scripts/build.sh
./scripts/run_bluezone.sh
```

`BlueZoneRunner` reads the properties file, resolves the configured adapters and
drivers via `ServiceLoader`, seeds the initial rate zones (`BLUE_ZONE`,
`ORANGE_ZONE`, `GREEN_ZONE`) and a 10% simulated payment failure rate, then runs
the selected driver(s).

**Web UI directly (Spring Boot):**

```bash
mvn -f src/bluezone-adapter-forparkingcars-webui/pom.xml spring-boot:run
```

Then open `http://localhost:8080`.

## Running with Docker

No Dockerfile is currently included. To containerize, package the web UI module as
an executable jar (`mvn -pl src/bluezone-adapter-forparkingcars-webui package`) and
run it with a standard `eclipse-temurin` base image, or contribute a Dockerfile via
a pull request.

## Environment variables

None required for the default (test-double) configuration. Spring Boot standard
properties (`server.port`, etc.) can be set in
`bluezone-adapter-forparkingcars-webui/src/main/resources/application.properties`
or overridden via the usual Spring Boot environment-variable conventions.

## API overview

The web UI exposes server-rendered pages rather than a JSON API:

| Route                | Controller                    | Purpose                          |
|-----------------------|--------------------------------|-----------------------------------|
| `/`                    | `MainController`               | Landing / navigation page        |
| `/purchase-ticket`     | `PurchaseTicketController`     | Purchase a ticket (form + result)|
| `/get-ticket`          | `GetTicketController`          | Look up a ticket by code         |

Errors are handled centrally by `GlobalExceptionHandler` and rendered via
`templates/error.html`.

## Database information

BlueZone ships with an **in-memory, non-persistent** ticket store
(`FakeTicketStoreAdapter`) and rate provider (`StubRateProviderAdapter`) — data does
not survive a restart. These exist to demonstrate the driven-port contract; swap in
a real database adapter by implementing `ForStoringTickets` / `ForObtainingRates`
and registering it in `ports-adapters.properties`.

## Testing instructions

**Cucumber acceptance tests** (parking use cases, Gherkin features under
`bluezone-driver-forparkingcars-test/src/main/resources/testcases`):

```bash
mvn -f src/bluezone-startup/pom.xml exec:java \
  -Dexec.args="scripts/ports-adapters.properties"
```
(with `ForParkingCars=test-cases` in the properties file)

**TestNG scenarios** (illegal-parking detection):

```bash
mvn -f src/bluezone-driver-forcheckingcars-test/pom.xml test
```

## Build instructions

```bash
mvn -f pom.xml clean verify
```

`verify` also triggers the `bluezone-startup` module's `maven-dependency-plugin`
executions, which assemble a runnable module-path directory
(`target/bzmodulepath`) containing all dependency jars and the startup jar.

## Development workflow

1. Define or extend a port in `bluezone-hexagon` if new capability is needed.
2. Implement the domain logic in the hexagon core.
3. Add or update an adapter module implementing the port.
4. Register the adapter in `ports-adapters.properties` (and its `module-info.java`
   `provides` clause).
5. Add Cucumber/TestNG coverage exercising the new behavior through a driver.
6. Run `mvn clean verify` from the root before committing.

## Future improvements

- Replace the in-memory adapters with real persistence (JPA/JDBC) and a real
  payment gateway integration.
- Expose a REST API alongside the server-rendered web UI.
- Add authentication/authorization for enforcement-officer use cases.
- Add a Dockerfile and docker-compose setup for one-command local startup.
- Externalize rate configuration instead of hardcoding it in
  `BlueZoneInitializer`.

## License

No license file is currently included. Add a `LICENSE` file to clarify usage terms
before distributing this project publicly.
