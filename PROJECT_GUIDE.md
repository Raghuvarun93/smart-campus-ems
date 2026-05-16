# Smart Campus Event Management System
## Complete Project Guide — Step-by-Step Execution

---

## PROJECT STRUCTURE
```
SmartCampusEMS/
├── pom.xml
└── src/
    └── main/
        ├── java/com/campus/ems/
        │   ├── SmartCampusEmsApplication.java      ← Main class + seed data
        │   ├── model/
        │   │   ├── Event.java                      ← @Entity with @NotNull, @Size
        │   │   ├── Student.java                    ← @Entity with @Email, @Pattern
        │   │   ├── Registration.java               ← @Entity for event registrations
        │   │   └── AppUser.java                    ← @Entity for admin/student login
        │   ├── repository/
        │   │   ├── EventRepository.java            ← JPA + JPQL + aggregate queries
        │   │   ├── StudentRepository.java
        │   │   ├── RegistrationRepository.java
        │   │   └── UserRepository.java
        │   ├── service/
        │   │   ├── EventService.java               ← Business logic + statistics
        │   │   ├── RegistrationService.java        ← Registration + feedback logic
        │   │   ├── StudentService.java
        │   │   └── CustomUserDetailsService.java   ← Spring Security integration
        │   ├── controller/
        │   │   ├── HomeController.java             ← @Controller, public routes
        │   │   ├── StudentController.java          ← @Controller, student routes
        │   │   ├── AdminController.java            ← @Controller, admin routes
        │   │   └── EventRestController.java        ← @RestController, REST API
        │   ├── config/
        │   │   └── SecurityConfig.java             ← Spring Security config
        │   └── exception/
        │       ├── ResourceNotFoundException.java
        │       ├── BusinessException.java
        │       └── GlobalExceptionHandler.java     ← @ControllerAdvice
        └── resources/
            ├── application.properties
            ├── static/
            │   ├── css/style.css
            │   └── js/app.js
            └── templates/
                ├── index.html                      ← Home page
                ├── login.html                      ← Login page
                ├── error.html                      ← Error page
                ├── fragments/layout.html           ← Navbar + footer fragments
                ├── student/
                │   ├── events.html                 ← Browse events
                │   ├── event-detail.html           ← Event details + register
                │   ├── register.html               ← Student registration form
                │   └── my-events.html              ← My events + feedback
                └── admin/
                    ├── dashboard.html              ← Admin dashboard
                    ├── events.html                 ← Manage events table
                    ├── event-form.html             ← Create/Edit event form
                    ├── registrations.html          ← View event registrations
                    ├── statistics.html             ← Charts + analytics
                    └── students.html               ← All students list
```

---

## STEP-BY-STEP EXECUTION GUIDE

---

### STEP 1 — INSTALL PREREQUISITES

Before running, install:

| Tool | Version | Download |
|------|---------|----------|
| JDK  | 17+     | https://adoptium.net |
| Maven | 3.8+  | https://maven.apache.org |
| IDE  | Any     | IntelliJ IDEA / VS Code / Eclipse |

Verify installation:
```bash
java -version        # Should show 17+
mvn -version         # Should show 3.8+
```

---

### STEP 2 — CREATE THE PROJECT FOLDER

Create the full folder structure manually OR use Spring Initializr:

**Option A — Manual (use this project)**
- Copy all provided files into the structure shown above.

**Option B — Spring Initializr (generate base)**
1. Go to https://start.spring.io
2. Fill:
   - Project: Maven
   - Language: Java
   - Spring Boot: 3.2.0
   - Group: com.campus | Artifact: smart-campus-ems
   - Java: 17
3. Add Dependencies:
   - Spring Web
   - Thymeleaf
   - Spring Data JPA
   - Spring Security
   - Validation
   - H2 Database
   - MySQL Driver
   - Lombok
   - Spring Boot DevTools
4. Click Generate → Download ZIP → Extract

---

### STEP 3 — COPY ALL PROJECT FILES

Copy every file exactly as provided:

```
src/main/java/com/campus/ems/
  ├── SmartCampusEmsApplication.java
  ├── model/           (Event, Student, Registration, AppUser)
  ├── repository/      (4 repository interfaces)
  ├── service/         (4 service classes)
  ├── controller/      (4 controllers)
  ├── config/          (SecurityConfig)
  └── exception/       (3 exception classes)

src/main/resources/
  ├── application.properties
  ├── static/css/style.css
  ├── static/js/app.js
  └── templates/       (all .html files)
```

---

### STEP 4 — CONFIGURE DATABASE

#### Option A: H2 In-Memory (Easiest — Default, no setup needed)
The `application.properties` is already configured for H2.
Data resets on every restart. Perfect for development/demo.

#### Option B: MySQL (Persistent data)
1. Install MySQL and open MySQL Workbench or terminal
2. Create the database:
```sql
CREATE DATABASE campus_ems;
```
3. In `application.properties`, comment out H2 section and uncomment MySQL:
```properties
# Comment these (H2 section)
# spring.datasource.url=jdbc:h2:mem:campusdb...

# Uncomment these (MySQL section)
spring.datasource.url=jdbc:mysql://localhost:3306/campus_ems?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```
4. Also comment out the H2 console line:
```properties
# spring.h2.console.enabled=true
```

---

### STEP 5 — BUILD THE PROJECT

Open terminal in the project root folder (where pom.xml is):

```bash
# Clean and compile
mvn clean compile

# Run tests (optional)
mvn test

# Package as JAR
mvn clean package -DskipTests
```

If you see `BUILD SUCCESS`, everything is correct.

---

### STEP 6 — RUN THE APPLICATION

#### Method A — Maven (easiest)
```bash
mvn spring-boot:run
```

#### Method B — JAR file
```bash
java -jar target/smart-campus-ems-1.0.0.jar
```

#### Method C — From IDE
- IntelliJ: Right-click `SmartCampusEmsApplication.java` → Run
- Eclipse: Right-click → Run As → Spring Boot App

---

### STEP 7 — VERIFY STARTUP

Watch the console. You should see:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
Started SmartCampusEmsApplication in 4.5 seconds
Tomcat started on port(s): 8080 (http)
```

If port 8080 is busy, change in `application.properties`:
```properties
server.port=9090
```

---

### STEP 8 — ACCESS THE APPLICATION

Open your browser and go to:

| Page | URL |
|------|-----|
| 🏠 Home | http://localhost:8080/ |
| 📅 Events | http://localhost:8080/events |
| 👤 Student Register | http://localhost:8080/student/register |
| 🔑 Login | http://localhost:8080/login |
| ⚙️ Admin Dashboard | http://localhost:8080/admin/dashboard |
| 🗄️ H2 Console | http://localhost:8080/h2-console |
| 🔌 REST API | http://localhost:8080/api/events |

---

### STEP 9 — LOGIN CREDENTIALS

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Student | student | student123 |

---

### STEP 10 — TEST ALL FEATURES

#### As Admin (login: admin / admin123):
1. Go to http://localhost:8080/login → login as admin
2. You land on Admin Dashboard
3. Click **Manage Events** → See all 5 sample events
4. Click **➕ New Event** → Fill form → Create
5. Click **✏️ Edit** on any event → Modify → Save
6. Click **👥 Registrations** → See who registered
7. Click **🗑️ Delete** → Confirm → Event deleted
8. Click **Statistics** → View charts (type/department breakdown)
9. Click **Search** → Filter by department, type, status, keyword

#### As Student:
1. Register: http://localhost:8080/student/register → Fill form
2. Browse events: http://localhost:8080/events
3. Click **View Details** on any event
4. Select your student profile → Click **Register Now**
5. Go to **My Events** → See your registrations
6. Give star rating feedback
7. Cancel a registration

#### REST API Testing (Postman or browser):
```
GET    http://localhost:8080/api/events           → All events (JSON)
GET    http://localhost:8080/api/events/upcoming  → Upcoming events
GET    http://localhost:8080/api/events/1         → Event by ID
GET    http://localhost:8080/api/events/search?department=CSE&eventType=Workshop
GET    http://localhost:8080/api/events/statistics
POST   http://localhost:8080/api/events           → Create (send JSON body)
PUT    http://localhost:8080/api/events/1         → Update
DELETE http://localhost:8080/api/events/1         → Delete
```

#### H2 Console (only for H2 mode):
1. Go to http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:campusdb`
3. Username: `sa` | Password: (empty)
4. Click Connect → Run SQL queries manually

---

### STEP 11 — COMMON ERRORS & FIXES

| Error | Cause | Fix |
|-------|-------|-----|
| `Port 8080 already in use` | Another app running | Change `server.port=9090` in properties |
| `Access Denied` | Not logged in | Login at /login first |
| `Could not load JDBC driver` | Wrong DB config | Check application.properties |
| `Table not found` | JPA not creating tables | Set `spring.jpa.hibernate.ddl-auto=create-drop` |
| `Thymeleaf template not found` | Missing HTML file | Check file path in templates/ folder |
| `Bean not found` | Missing @Autowired target | Ensure @Service / @Repository annotations present |
| Maven build fails | Dependency issue | Run `mvn clean install -U` to force update |

---

## TECHNICAL HIGHLIGHTS SUMMARY

| Spring Concept | Where Used |
|----------------|-----------|
| `@SpringBootApplication` | SmartCampusEmsApplication.java |
| `@Autowired` (DI) | All Service and Controller classes |
| `@Controller` + `@RequestMapping` | StudentController, AdminController, HomeController |
| `@RestController` | EventRestController |
| `@Entity`, `@Id`, `@Table` | Event, Student, Registration, AppUser |
| `@OneToMany`, `@ManyToOne` | Event ↔ Registration ↔ Student |
| `@NotBlank`, `@Size`, `@Email`, `@Future` | Event.java, Student.java |
| `@Valid`, `BindingResult` | All form-handling POST methods |
| `@ControllerAdvice` | GlobalExceptionHandler.java |
| `@Query` (JPQL) | EventRepository, RegistrationRepository |
| `Spring Security` | SecurityConfig.java, CustomUserDetailsService |
| `Thymeleaf` | All HTML templates |
| `CommandLineRunner` | Seed data in main application class |
| `Chart.js` | Statistics page charts |

---

## REST API ENDPOINTS REFERENCE

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/events | All events | Public |
| GET | /api/events/upcoming | Upcoming only | Public |
| GET | /api/events/{id} | Event by ID | Public |
| GET | /api/events/search | Filter search | Public |
| GET | /api/events/statistics | Aggregate stats | Public |
| POST | /api/events | Create event | Admin |
| PUT | /api/events/{id} | Update event | Admin |
| DELETE | /api/events/{id} | Delete event | Admin |

---

*Smart Campus EMS — SDG 9: Industry, Innovation & Infrastructure*
*Built with Spring Boot 3.2 + Spring Data JPA + Thymeleaf + Spring Security*
