# Implementation Plan: Smart Campus Event Management System

## Overview

The application skeleton is substantially complete. All domain models, repositories, services, controllers, Thymeleaf templates, security configuration, and seed data are in place. The remaining work falls into four areas:

1. **Bug fixes and completeness gaps** in the existing production code (keyword search covers title only, not description; seed data missing `email` on AppUser; `EventService.searchEvents` 4-arg overload used by REST controller lacks `dateFrom`/`dateTo` forwarding).
2. **Test infrastructure** — no `src/test` directory exists; jqwik is not in `pom.xml`.
3. **Unit and property-based tests** for all service-layer correctness properties defined in the design.
4. **Integration / security tests** for controllers, REST endpoints, and the global exception handler.

Tasks are ordered so each builds on the previous. All test sub-tasks are marked optional (`*`) and must not be auto-implemented.

---

## Tasks

- [x] 1. Fix production code gaps and add jqwik dependency
  - [x] 1.1 Add jqwik dependency to `pom.xml`
    - Add `net.jqwik:jqwik:1.8.2` with `<scope>test</scope>` inside `<dependencies>`
    - _Requirements: Design – Testing Strategy_

  - [x] 1.2 Fix `EventRepository.searchEvents` keyword filter to also match `description`
    - In `EventRepository`, update the JPQL `searchEvents` query so the keyword clause reads:
      `(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%',:keyword,'%')))`
    - _Requirements: 10.3_

  - [x] 1.3 Fix `AppUser` seed data — set `email` field on both seed users
    - In `SmartCampusEmsApplication.seedData`, set `admin.setEmail("admin@campus.edu")` and `studentUser.setEmail("student@campus.edu")` before saving, so the `UNIQUE` constraint on `app_users.email` is satisfied and the email-based student-profile lookup works for the demo student account
    - _Requirements: 16.1, 16.2_

  - [x] 1.4 Verify `EventRestController.searchEvents` forwards all four parameters correctly
    - The REST `GET /api/events/search` handler calls `eventService.searchEvents(department, eventType, status, keyword)` (4-arg overload). Confirm the 4-arg overload in `EventService` delegates to the 6-arg overload with `null, null` for date range — this is already the case; add a comment confirming the delegation is intentional
    - _Requirements: 10.5_

  - [x] 1.5 Checkpoint — build the project and confirm it compiles and starts cleanly
    - Run `mvn clean package -DskipTests` and verify `BUILD SUCCESS`
    - Start the application and confirm the H2 console is reachable at `http://localhost:8080/h2-console`
    - _Requirements: 16.1, 16.2, 16.3, 16.4_

- [x] 2. Set up test infrastructure
  - [x] 2.1 Create the Maven test source tree and base test configuration
    - Create directory `src/test/java/com/campus/ems/`
    - Create `src/test/resources/application-test.properties` with H2 in-memory datasource, `spring.jpa.hibernate.ddl-auto=create-drop`, and `spring.security.enabled=true`
    - _Requirements: Design – Testing Strategy_

  - [x] 2.2 Create a shared `TestDataFactory` helper class
    - Create `src/test/java/com/campus/ems/TestDataFactory.java`
    - Provide static factory methods: `makeEvent(String title, String status, int capacity)`, `makeStudent(String name, String email)`, `makeUser(String username, String role)`, `makeRegistration(Student s, Event e)`
    - These helpers are used by all test classes to avoid repetition
    - _Requirements: Design – Testing Strategy_

- [-] 3. Smoke tests — seed data verification
  - [-] 3.1 Create `SeedDataTest` to verify startup data
    - Create `src/test/java/com/campus/ems/smoke/SeedDataTest.java` as a `@SpringBootTest` integration test
    - Assert: `userRepository.findByUsername("admin")` is present with `role == "ROLE_ADMIN"` (Req 16.1)
    - Assert: `userRepository.findByUsername("student")` is present with `role == "ROLE_STUDENT"` (Req 16.2)
    - Assert: `studentRepository.count() >= 3` (Req 16.3)
    - Assert: `eventRepository.findByStatusOrderByEventDateAsc("UPCOMING").size() >= 5` (Req 16.4)
    - _Requirements: 16.1, 16.2, 16.3, 16.4_

- [-] 4. `UserService` tests — Properties 1 and 2
  - [-] 4.1 Create `UserServiceTest` with mocked repositories
    - Create `src/test/java/com/campus/ems/service/UserServiceTest.java`
    - Use `@ExtendWith(MockitoExtension.class)` and inject mocked `UserRepository`, `StudentRepository`, `PasswordEncoder`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [ ]* 4.2 Write property test for Property 1 — invalid signup is always rejected
    - **Property 1: User registration rejects invalid credentials**
    - **Validates: Requirements 1.1, 1.3**
    - Use `@Property(tries = 200)` with `@ForAll @StringLength(max = 5) String shortPassword` to verify `registerNewUser` throws `BusinessException` for passwords shorter than 6 chars
    - Use a separate `@Property` with a pre-existing username to verify duplicate-username rejection; assert no `AppUser` or `Student` is saved (verify `userRepository.save` is never called)
    - _Requirements: 1.2, 1.4_

  - [ ]* 4.3 Write property test for Property 2 — successful registration produces correct role and BCrypt hash
    - **Property 2: Registered users have correct role and hashed password**
    - **Validates: Requirements 1.2**
    - Use `@Property(tries = 100)` with valid generated inputs; capture the `AppUser` passed to `userRepository.save`; assert `role == "ROLE_STUDENT"` and `BCryptPasswordEncoder.matches(rawPassword, savedPassword) == true`
    - _Requirements: 1.1, 1.5_

- [-] 5. `StudentService` tests — Properties 4, 5, and 16
  - [-] 5.1 Create `StudentServiceTest` with mocked `StudentRepository`
    - Create `src/test/java/com/campus/ems/service/StudentServiceTest.java`
    - Use `@ExtendWith(MockitoExtension.class)`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 13.2_

  - [ ]* 5.2 Write property test for Property 4 — student validation constraints are universally enforced
    - **Property 4: Student validation constraints are universally enforced**
    - **Validates: Requirements 3.1, 3.3, 14.1**
    - Use Jakarta `Validator` directly (no Spring context needed); generate `Student` objects with `@ForAll` name strings outside 2–100 chars, invalid email formats, and phone strings not matching `^[0-9]{10}$`; assert `validator.validate(student)` returns a non-empty `ConstraintViolation` set
    - _Requirements: 3.3, 3.4, 3.5_

  - [ ]* 5.3 Write property test for Property 5 — duplicate student email is always rejected
    - **Property 5: Duplicate student email is always rejected**
    - **Validates: Requirements 3.2**
    - Use `@Property(tries = 100)` with `@ForAll @Email String email`; stub `studentRepository.existsByEmail(email)` to return `true`; assert `registerStudent` throws `BusinessException` and `studentRepository.save` is never called
    - _Requirements: 3.2_

  - [ ]* 5.4 Write property test for Property 16 — student search filters are conjunctively applied
    - **Property 16: Student search filters are conjunctively applied**
    - **Validates: Requirements 13.1, 13.2**
    - Build a list of `Student` objects with varied names, emails, departments, and years; stub `studentRepository.searchStudents` to delegate to an in-memory filter; use `@Property(tries = 100)` with generated filter combinations; assert every returned student satisfies all non-null filter conditions and keyword matching is case-insensitive
    - _Requirements: 13.2_

- [-] 6. `EventService` tests — Properties 6, 7, 13, 14, and 15
  - [-] 6.1 Create `EventServiceTest` with mocked `EventRepository`
    - Create `src/test/java/com/campus/ems/service/EventServiceTest.java`
    - Use `@ExtendWith(MockitoExtension.class)`
    - _Requirements: 4.1, 4.2, 9.1, 9.2, 12.1_

  - [ ]* 6.2 Write property test for Property 6 — public event listing contains only UPCOMING events in date order
    - **Property 6: Public event listing contains only UPCOMING events in date order**
    - **Validates: Requirements 4.1**
    - Use `@Property(tries = 200)` with `@ForAll List<Event> events` containing mixed statuses and random dates; stub `eventRepository.findByStatusOrderByEventDateAsc("UPCOMING")` to return the filtered+sorted subset; assert every element has `status == "UPCOMING"` and the list is sorted by `eventDate` ascending
    - _Requirements: 4.1_

  - [ ]* 6.3 Write property test for Property 7 — event search filters are conjunctively applied
    - **Property 7: Event search filters are conjunctively applied**
    - **Validates: Requirements 4.2, 10.1, 10.2**
    - Use `@Property(tries = 100)` with generated filter combinations (department, eventType, status, keyword); stub `eventRepository.searchEvents` to delegate to an in-memory predicate; assert every returned event satisfies all non-null filters and keyword matching is case-insensitive against both `title` and `description`
    - _Requirements: 10.3_

  - [ ]* 6.4 Write property test for Property 13 — event validation constraints are universally enforced
    - **Property 13: Event validation constraints are universally enforced**
    - **Validates: Requirements 9.1, 14.1**
    - Use Jakarta `Validator` directly; generate `Event` objects with title outside 3–150 chars, description outside 10–1000 chars, capacity outside 1–1000, and blank required fields; assert `validator.validate(event)` returns a non-empty violation set
    - _Requirements: 9.2, 14.1, 14.2_

  - [ ]* 6.5 Write property test for Property 14 — new events default to UPCOMING status
    - **Property 14: New events default to UPCOMING status**
    - **Validates: Requirements 9.2**
    - Use `@Property(tries = 100)` with valid `Event` objects where `status` is null or blank; call `eventService.saveEvent(event)`; capture the argument passed to `eventRepository.save`; assert `status == "UPCOMING"`
    - _Requirements: 9.1_

  - [ ]* 6.6 Write property test for Property 15 — statistics accurately reflect stored data
    - **Property 15: Statistics accurately reflect stored data**
    - **Validates: Requirements 12.1**
    - Use `@Property(tries = 100)` with a generated list of events; stub `eventRepository.count()`, `totalRegistrations()`, and `findByStatusOrderByEventDateAsc("UPCOMING")`; call `eventService.getStatistics()`; assert `totalEvents == eventRepository.count()`, `totalRegistrations == stubbed sum`, and `upcomingCount == stubbed upcoming list size`
    - _Requirements: 12.1_

- [-] 7. `RegistrationService` tests — Properties 8, 9, 10, 11, and 12
  - [-] 7.1 Create `RegistrationServiceTest` with mocked repositories
    - Create `src/test/java/com/campus/ems/service/RegistrationServiceTest.java`
    - Use `@ExtendWith(MockitoExtension.class)`; inject mocked `RegistrationRepository`, `EventRepository`, `StudentRepository`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 7.1, 7.2, 7.3, 8.1, 8.2_

  - [ ]* 7.2 Write property test for Property 8 — successful registration increments count and sets timestamp
    - **Property 8: Successful registration increments count and sets timestamp**
    - **Validates: Requirements 5.1, 5.3**
    - Use `@Property(tries = 100)` with generated `(studentId, eventId, initialCount, capacity)` where `initialCount < capacity`; stub an UPCOMING event with that count/capacity and no existing registration; call `registerStudentForEvent`; capture the `Event` saved to `eventRepository`; assert `savedEvent.registeredCount == initialCount + 1` and the returned `Registration.registeredAt` is non-null
    - _Requirements: 5.1, 5.5_

  - [ ]* 7.3 Write property test for Property 9 — registration business rules are universally enforced
    - **Property 9: Registration business rules are universally enforced**
    - **Validates: Requirements 5.2**
    - Write three `@Property` methods:
      1. Already-registered: stub `existsByStudentIdAndEventId` to return `true`; assert `BusinessException` is thrown and `registeredCount` is unchanged
      2. Full event: stub event with `registeredCount == totalCapacity`; assert `BusinessException` is thrown
      3. Non-UPCOMING event: stub event with status `COMPLETED`, `ONGOING`, or `CANCELLED`; assert `BusinessException` is thrown
    - _Requirements: 5.2, 5.3, 5.4_

  - [ ]* 7.4 Write property test for Property 10 — cancellation decrements count and never goes below zero
    - **Property 10: Cancellation decrements count and never goes below zero**
    - **Validates: Requirements 7.1, 7.3**
    - Use `@Property(tries = 100)` with generated `initialCount` in range `[0, 100]`; stub a CONFIRMED registration linked to an event with that count; call `cancelRegistration`; assert `registration.status == "CANCELLED"` and `event.registeredCount == Math.max(0, initialCount - 1)`
    - _Requirements: 7.1, 7.3_

  - [ ]* 7.5 Write property test for Property 11 — double cancellation is always rejected
    - **Property 11: Double cancellation is always rejected**
    - **Validates: Requirements 7.2**
    - Use `@Property(tries = 100)`; stub a registration with `status == "CANCELLED"`; assert `cancelRegistration` throws `BusinessException` and `eventRepository.save` is never called
    - _Requirements: 7.2_

  - [ ]* 7.6 Write property test for Property 12 — feedback validation and idempotence
    - **Property 12: Feedback validation and idempotence**
    - **Validates: Requirements 8.1, 8.2**
    - Part A: Use Jakarta `Validator` on `Registration` objects with `feedbackRating` outside [1, 5] or `feedbackComment` longer than 500 chars; assert violations are reported
    - Part B: Use `@Property(tries = 100)`; stub a registration with a non-null `feedbackRating`; assert `submitFeedback` throws `BusinessException`
    - _Requirements: 8.2, 8.3_

- [ ] 8. Checkpoint — run all unit and property tests
  - Run `mvn test` and ensure all tests in `service/` and `smoke/` pass
  - Fix any failures before proceeding to integration tests
  - _Requirements: All service-layer requirements_

- [ ] 9. Security and controller integration tests — Properties 3, 17, and 18
  - [ ] 9.1 Create `SecurityAccessTest` using MockMvc and Spring Security test support
    - Create `src/test/java/com/campus/ems/controller/SecurityAccessTest.java`
    - Annotate with `@SpringBootTest` and `@AutoConfigureMockMvc`
    - _Requirements: 2.3, 2.4, 2.5, 2.7_

  - [ ]* 9.2 Write property test for Property 3 — admin URLs are inaccessible to students
    - **Property 3: Admin URLs are inaccessible to students**
    - **Validates: Requirements 2.2**
    - Use `@Property(tries = 50)` with `@ForAll @From("adminPaths") String path` (provide an `@Provide` method returning a sample of `/admin/**` paths); perform `mockMvc.perform(get(path).with(user("student").roles("STUDENT")))` and assert the response status is either 403 or a redirect to `/login`
    - Also assert that `GET /admin/dashboard` with `ROLE_ADMIN` returns HTTP 200
    - _Requirements: 2.3, 2.5_

  - [ ]* 9.3 Write unit tests for public URL access (unauthenticated)
    - Assert `GET /events` returns HTTP 200 without authentication
    - Assert `GET /api/events` returns HTTP 200 without authentication
    - Assert `GET /student/my-events` without authentication redirects to `/login`
    - _Requirements: 2.3, 2.4, 2.7_

  - [ ] 9.4 Create `EventRestControllerTest` for REST API validation
    - Create `src/test/java/com/campus/ems/controller/EventRestControllerTest.java`
    - Annotate with `@SpringBootTest` and `@AutoConfigureMockMvc`
    - _Requirements: 14.3, 14.5_

  - [ ]* 9.5 Write property test for Property 17 — REST API returns HTTP 400 for invalid payloads
    - **Property 17: REST API returns HTTP 400 for invalid payloads**
    - **Validates: Requirements 14.3**
    - Use `@Property(tries = 100)` with generated invalid `Event` JSON payloads (blank title, capacity = 0, missing required fields); perform `mockMvc.perform(post("/api/events").contentType(APPLICATION_JSON).content(json))`; assert response status is 400 and the body contains an `"errors"` key
    - _Requirements: 14.5_

  - [ ] 9.6 Create `GlobalExceptionHandlerTest` for error page rendering
    - Create `src/test/java/com/campus/ems/controller/GlobalExceptionHandlerTest.java`
    - Annotate with `@SpringBootTest` and `@AutoConfigureMockMvc`
    - _Requirements: 15.1, 15.2, 15.3_

  - [ ]* 9.7 Write property test for Property 18 — exception handler maps exception types to correct error codes
    - **Property 18: Exception handler maps exception types to correct error codes**
    - **Validates: Requirements 15.1**
    - Test `GET /events/999999` (non-existent ID) and assert the response body contains `errorCode` value `"404"` (rendered by `error.html`)
    - Test a business-rule violation path (e.g., registering for a full event via MockMvc) and assert `errorCode == "400"` in the rendered response
    - _Requirements: 15.1, 15.2_

- [ ] 10. Final checkpoint — full test suite
  - Run `mvn clean test` and confirm all tests pass
  - Verify the application starts with `mvn spring-boot:run` and seed data is visible in the H2 console
  - Ensure all Thymeleaf templates render without errors by manually navigating key pages: `/`, `/events`, `/login`, `/admin/dashboard`, `/admin/statistics`
  - _Requirements: All_

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints (tasks 1.5, 8, 10) ensure incremental validation before moving to the next layer
- Property tests use jqwik `@Property` + `@ForAll`; each runs a minimum of 100 iterations by default
- Unit tests use JUnit 5 + Mockito (already on the classpath via `spring-boot-starter-test`)
- Integration tests use `@SpringBootTest` + `MockMvc` with the H2 in-memory database
- The `application-test.properties` profile keeps test data isolated from the dev H2 instance
