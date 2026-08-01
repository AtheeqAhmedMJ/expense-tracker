# Smart Expense Tracker API

A REST API for tracking personal expenses, built with Spring Boot 3.5.4 and Java 21.

## What it does

- Add an expense (title, amount, category, date)
- View all expenses
- Filter expenses by category
- Search expenses by title keyword
- Calculate total expenses (overall and by category, via the summary endpoint)
- Monthly expense summary (grouped by year/month)
- Update and delete an expense
- OpenAPI/Swagger docs for exploring the API

Data is stored **in memory** (a thread-safe `ConcurrentHashMap`) — there is no
database to set up. Restarting the app clears all data, by design.

## Prerequisites

- Java 21 (Temurin/OpenJDK). Check with:
  ```bash
  java -version
  ```
- No other services required. No database, no Docker.

> If you have multiple JDKs installed and `java -version` doesn't show 21,
> either switch your active JDK to 21 before building, or point `JAVA_HOME`
> at a Java 21 install. This project will not build correctly on Java
> versions other than 21.

## Install

From the project root:

```bash
./mvnw clean install
```

(On Windows: `mvnw.cmd clean install`)

This compiles the code, generates the MapStruct mapper implementation, and
runs the test suite as part of the build.

## Run the server

```bash
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080** by default unless explicitly told not to do so.

## Run the tests only

```bash
./mvnw test
```

## Project Structure

```text
SmartExpenseTracker/
├── README.md
├── AI_NOTES.md
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/diligent/expensetracker/
│   │   │   ├── config/
│   │   │   │   └── MapperConfig.java
│   │   │   ├── controller/
│   │   │   │   └── ExpenseController.java
│   │   │   ├── dto/
│   │   │   │   ├── CategorySummaryResponse.java
│   │   │   │   ├── ExpenseRequest.java
│   │   │   │   ├── ExpenseResponse.java
│   │   │   │   ├── ExpenseSummaryResponse.java
│   │   │   │   └── MonthlySummaryResponse.java
│   │   │   ├── entity/
│   │   │   │   └── Expense.java
│   │   │   ├── enums/
│   │   │   │   └── ExpenseCategory.java
│   │   │   ├── exception/
│   │   │   │   ├── ExpenseNotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── mapper/
│   │   │   │   └── ExpenseMapper.java
│   │   │   ├── repository/
│   │   │   │   └── ExpenseRepository.java
│   │   │   ├── response/
│   │   │   │   └── ApiResponse.java
│   │   │   ├── service/
│   │   │   │   ├── ExpenseService.java
│   │   │   │   └── ExpenseServiceImpl.java
│   │   │   └── ExpensetrackerApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/diligent/expensetracker/
│           ├── controller/
│           │   └── ExpenseControllerTest.java
│           ├── exception/
│           │   └── GlobalExceptionHandlerTest.java
│           ├── mapper/
│           │   └── ExpenseMapperTest.java
│           ├── repository/
│           │   └── ExpenseRepositoryTest.java
│           ├── service/
│           │   └── ExpenseServiceImplTest.java
│           ├── ExpenseApiIntegrationTest.java
│           └── ExpensetrackerApplicationTests.java
└── tests/
    └── README.md
```
# Tests

This project follows the standard Maven/Spring Boot directory layout.

All test sources are located under:

```text
src/test/java/
```

Run the complete test suite using:

```bash
./mvnw test
```

The `tests/` directory exists to match the repository structure requested in the assignment instructions. Actual test classes remain under `src/test`, which is the standard Maven convention.

### Test layout

Tests mirror the `src/main` package structure, one focused suite per layer,
instead of one large end-to-end file:

```
src/test/java/com/diligent/expensetracker/
├── ExpensetrackerApplicationTests.java   # Spring context loads
├── ExpenseApiIntegrationTest.java        # full-stack smoke test, real beans
├── controller/ExpenseControllerTest.java # @WebMvcTest, service mocked
├── service/ExpenseServiceImplTest.java   # Mockito, repository+mapper mocked
├── repository/ExpenseRepositoryTest.java # plain JUnit, no Spring context
├── mapper/ExpenseMapperTest.java         # MapStruct-generated mapper
└── exception/GlobalExceptionHandlerTest.java
```

Run a single layer while iterating, e.g.:

```bash
./mvnw test -Dtest=ExpenseServiceImplTest
./mvnw test -Dtest="com.diligent.expensetracker.controller.*"
```

## Explore the API (Swagger UI)

Once the server is running, open:

```
http://localhost:8080/swagger-ui/index.html#/
```

This gives an interactive view of every endpoint, request/response shapes,
and lets you try requests directly from the browser.

## API endpoints

| Method | Path                                  | Description                          |
|--------|----------------------------------------|--------------------------------------|
| POST   | `/api/v1/expenses`                     | Create an expense                    |
| GET    | `/api/v1/expenses`                     | List all expenses                    |
| GET    | `/api/v1/expenses/{id}`                | Get a single expense                 |
| PUT    | `/api/v1/expenses/{id}`                | Update an expense                    |
| DELETE | `/api/v1/expenses/{id}`                | Delete an expense                    |
| GET    | `/api/v1/expenses/category/{category}` | Filter by category                   |
| GET    | `/api/v1/expenses/search?keyword=...`  | Search by title keyword              |
| GET    | `/api/v1/expenses/summary`             | Overall total + transaction count    |
| GET    | `/api/v1/expenses/monthly-summary`     | Totals grouped by year/month         |

Valid `category` values: `FOOD`, `TRAVEL`, `HEALTH`, `SHOPPING`,
`ENTERTAINMENT`, `OTHER`.

### Example request

```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Coffee",
        "amount": 150.00,
        "category": "FOOD",
        "expenseDate": "2026-08-01"
      }'
```

## Bonus feature chosen

The chosen bonus is **OpenAPI/Swagger documentation** (`springdoc-openapi`),
since it directly helps a reviewer explore and try the API without reading
the source first.

The search and monthly-summary endpoints were also implemented while
exploring the domain model, but these are extras built alongside the main
CRUD requirements : not the chosen bonus.

## Notes on design

- **No database, by design.** The assignment explicitly allows in-memory or
  local-JSON storage, so this uses a `ConcurrentHashMap`-backed repository
  (`ExpenseRepository`) with an `AtomicLong` ID generator — no Postgres,
  Flyway, or Docker required to run this.
- **Layering:** Controller → Service → Repository, with DTOs (Java records)
  for request/response shapes, a MapStruct-generated mapper between the
  entity and DTOs, and a `@RestControllerAdvice` global exception handler
  for consistent error responses.
- **Validation:** Bean Validation annotations (`@NotBlank`, `@NotNull`,
  `@Positive`) on `ExpenseRequest` reject invalid input with a 400 before it
  reaches the service layer.

See `AI_NOTES.md` for details on how AI tools were used while building this.
