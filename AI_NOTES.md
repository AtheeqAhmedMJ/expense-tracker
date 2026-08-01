# AI_NOTES.md

I used two AI tools during development:
- **ChatGPT:** for project planning, architecture discussions, scope decisions, trade-off analysis, and troubleshooting.
- **Claude:** for implementation scaffolding, boilerplate code, unit tests, and build-issue diagnosis.

---

# 1. Which Parts Were AI-Generated vs. Written by Me

## 1.1. Which Parts Were AI-Generated

Before writing any code, I used ChatGPT to plan the architecture and decide how the project should be structured and how best to satisfy the requirements without unnecessary complexity.

I discussed whether to include a frontend alongside the backend, database vs. in-memory repository comparison, repository design and package structure, layer separation, future extensibility, testing strategy, and finally API organization.

My original plan was to include a React frontend to thoroughly test the application. However, after the discussion, I decided against it because the assignment evaluates only the REST API.

Instead of building a React application, I created a simple HTML and CSS interface to perform basic interaction, verify API responses, measure response times, and support white-box testing.

### Project architecture

Before creating any classes, I planned the package layout, which remained unchanged throughout development:

- controller
- service
- repository
- dto
- entity
- mapper
- exception
- response
- enums
- config

I kept the codebase focused, making it easier to review, test, and maintain.

### Development troubleshooting

I used ChatGPT for smaller technical issues:

- Java 21 vs. newer JDK versions
- Maven compiler configuration
- Apple Silicon architecture verification
- curl testing ideas
- Swagger configuration questions

I also used Claude during the testing phase of this project.

I used it to generate more than 25 test cases and edge-case scenarios to thoroughly validate the REST API and ensure it returned the correct HTTP status codes, including **200 OK**, **201 Created**, **400 Bad Request**, **404 Not Found**, and **500 Internal Server Error**.

I manually executed all API test scenarios using cURL to verify endpoint behaviour, request validation, error handling, and response times. These manual tests complemented the automated tests, helping me identify defects, verify exception handling, and confirm that the API returned the expected status codes and response payloads under both normal and edge-case conditions.

A major area of focus was the `GlobalExceptionHandler`, where I refined the exception-handling logic so that validation failures, malformed request bodies, missing parameters, type mismatches, missing resources, and unexpected server errors all returned consistent, meaningful API responses.

I also used Claude to help generate a comprehensive suite of JUnit test classes. These included dedicated tests for the controller, service, repository, mapper, and exception layers, as well as integration tests (`ExpenseApiIntegrationTest`) and the default Spring Boot application test (`ExpensetrackerApplicationTests`). I reviewed every generated test, modified them where necessary, executed them locally, and used the results to identify defects and verify that the application behaved as expected before considering the implementation complete.

---

## 1.2. Written and Decided by Me

I wrote every line of production code in this project myself. I prefer a traditional, hands-on development approach, so I used IntelliJ IDEA's code intelligence, navigation, and refactoring tools rather than relying on AI to generate implementation code.

Before writing any code, I planned the application architecture using OOSE principles and UML diagrams to map the relationships between classes and define the responsibilities of each layer. This helped me organize the project into a clean Controller, Service, Repository, Mapper, DTO, Entity, and Exception structure while maintaining separation of concerns.

I also designed the REST API, including the endpoints, request and response models, validation rules, HTTP status codes, and overall project scope. I also decided which features to implement, how to organize the packages, which dependencies to include, and how to configure the Spring Boot application.

I also implemented the business logic, repository behavior, and exception handling, and debugged build failures, dependency issues, runtime errors, and API behavior during development.

I also determined the overall testing strategy, reviewed every AI-generated suggestion, modified or rejected code that did not fit the project's design, and made the final decisions on every feature and implementation before submission.

---

# 2. What I Validated, Tested, or Changed and Why

### Java Version Investigation

Repeated Maven builds revealed an intermittent issue: Maven reported **BUILD SUCCESS**, but the application failed to start with an `UnsupportedClassVersionError`.

I investigated further using `javap -verbose` to inspect the generated class files directly. This confirmed that some classes were unexpectedly compiled with Java 25, even though the project targeted Java 21. I traced the cause to a VS Code extension that had partially migrated the project to Java 25 in the background, not a Maven issue at all.

### Repository Validation

After replacing JPA with an in-memory implementation, I manually checked every repository method against the service layer, including:

- save
- findById
- delete
- findAll
- search
- category filtering
- expense totals
- count
- monthly summary

This confirmed there were no interface mismatches after the storage change.

### Swagger Validation

Adding the dependency alone wasn't sufficient. I confirmed that the Swagger UI loaded, the documentation was generated correctly, every endpoint appeared, and the request/response schemas were accurate.


### Cleanup

I removed temporary files created during debugging (`effective-pom.xml`, dependency reports, temporary Maven outputs), since these are generated artifacts that shouldn't be committed to source control.

### Manual API Testing

I manually exercised every endpoint using Postman and curl:

- Create Expense
- Get Expense
- Update Expense
- Delete Expense
- List Expenses
- Search
- Filter by Category
- Total Summary
- Monthly Summary

I also verified validation failures involving:

- missing required fields
- invalid enum values
- malformed requests
- missing IDs
- non-existent resources

Only after these tests passed did I consider the implementation complete.

### Test Suite Improvements

The original project contained a single large controller test.

Claude suggested reorganizing it into smaller, focused units, resulting in separate tests for the Repository, Mapper, Service, Controller, and Global Exception Handler, plus a lightweight integration test.

Since Claude generated these without running Maven, I compiled and ran the full suite locally myself, fixing any compilation or assertion failures before accepting them.

---

# 3. AI Suggestions I Decided Not to Use and Why

### Bonus Feature Selection

The assignment allows only one official bonus feature.

I considered several options:

- Swagger
- Docker
- Authentication
- Search
- Monthly Summary

I deliberately chose **Swagger/OpenAPI** because it provides immediate value to reviewers while carrying very little implementation risk.

### Features I Rejected

While discussing possible optimizations with AI, it suggested adding:

- pagination
- sorting
- caching
- asynchronous processing
- several production-oriented technologies

After thinking through each suggestion, I decided they didn't add meaningful value to this assignment and would only increase the project's complexity.

I chose not to implement pagination and sorting because the application stores a relatively small number of records in memory, making both features unnecessary.

For the same reason, I also decided against adding caching and asynchronous processing, as the performance benefits would be negligible for an application of this size.

AI also recommended using PostgreSQL with Spring Data JPA, but I deliberately opted for an in-memory repository because the assignment explicitly allows it, and using a database would add unnecessary configuration without improving the solution.

I similarly chose not to implement JWT authentication or Spring Security since authentication falls outside the scope of the assignment.

Although the project includes features such as search and monthly summaries, I documented only Swagger/OpenAPI as the official bonus feature to stay aligned with the assignment requirements rather than claiming multiple bonuses.

I also explored using Docker and even looked into installing Docker Desktop while planning the project.

In the end, I decided not to include Docker because the application can be built and run directly with Maven, making containerization unnecessary for reviewers. Docker is far more beneficial for larger, production-scale applications where consistent deployment environments matter, whereas for this assignment, it would have added extra setup without providing any real advantage.

AI substantially accelerated development by assisting with planning, scaffolding, documentation, testing, and debugging. However, I never used it as a substitute for my own engineering judgment. I reviewed, compiled, executed, tested, and modified every generated implementation before accepting it into the final project.

The important architectural decisions, including keeping the project backend-only, choosing an in-memory repository, selecting Swagger as the official bonus feature, rejecting unnecessary technologies, restructuring the tests, and resolving build issues, were made by me based on the assignment requirements rather than by blindly accepting AI-generated suggestions.