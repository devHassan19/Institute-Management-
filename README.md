# Institute Management System

## Project Description

The **Institute Management System** is a RESTful backend application designed to manage the main operations of an educational institute.

The system provides functionality for managing users, students, instructors, courses, classes, enrollments, notifications, and audit logs. It also includes authentication and authorization using JWT and role-based access control.

The application is designed to provide a secure and organized API that allows different users to access functionality according to their roles and permissions.

## Application Purpose

The purpose of this project is to develop a centralized system for managing an educational institute and its daily operations.

The system allows administrators to manage institute resources such as instructors, courses, classes, users, and enrollments, while students can access their own information and manage their course enrollments.

## Main Features

* User registration and authentication
* JWT-based authentication
* Role-based authorization
* Email/account verification
* Password reset functionality
* Password change functionality
* Student management
* Instructor management
* Course management
* Class management
* Student enrollment management
* Personal enrollment management
* Real-time notifications using Server-Sent Events (SSE)
* Audit logging
* Pagination and sorting for classes
* PostgreSQL database integration
* RESTful API
* Swagger / OpenAPI documentation

---

## Technologies

The project was developed using the following technologies:

* **Java**
* **Spring Boot**
* **Spring Security**
* **JWT**
* **Spring Data JPA**
* **Hibernate**
* **PostgreSQL**
* **Maven**
* **Swagger / OpenAPI**
* **Server-Sent Events (SSE)**
* **Lombok**
* **Git**
* **GitHub**

---

## Architecture

The application follows a layered architecture to separate responsibilities between different parts of the system.

### Main Layers

**Controller Layer**

Handles HTTP requests and responses and exposes the REST API endpoints.

**Service Layer**

Contains the main business logic and handles operations between controllers and repositories.

**Repository Layer**

Uses Spring Data JPA to communicate with the PostgreSQL database.

**Model / Entity Layer**

Contains the JPA entities representing the main database tables and relationships.

**Security Layer**

Handles authentication and authorization using Spring Security and JWT.

### Application Structure

```text
src/main/java/com/example/institute/institute
│
├── controller
│   ├── UserController
│   ├── StudentController
│   ├── InstructorController
│   ├── CourseController
│   ├── ClassController
│   ├── EnrollmentController
│   ├── NotificationController
│   └── AuditLogController
│
├── service
│   ├── UserService
│   ├── StudentService
│   ├── InstructorService
│   ├── CourseService
│   ├── ClassService
│   ├── EnrollmentService
│   └── ...
│
├── repository
│   ├── UserRepository
│   ├── StudentRepository
│   ├── InstructorRepository
│   ├── CourseRepository
│   ├── ClassRepository
│   ├── EnrollmentRepository
│   └── ...
│
├── model
│   ├── User
│   ├── UserProfile
│   ├── Student
│   ├── Instructor
│   ├── Course
│   ├── Class
│   ├── Enrollment
│   └── AuditLog
│
└── security
    ├── JwtRequestFilter
    ├── JwtUtil
    ├── MyUserDetails
    └── SecurityConfig
```

---

## General Approach

The project was developed using a layered Spring Boot architecture where each component has a specific responsibility. Controllers handle API requests, services contain the business logic, repositories communicate with PostgreSQL through Spring Data JPA, and entities represent the application's data model.

Authentication and authorization were implemented using Spring Security and JWT. Users authenticate through the login endpoint and receive a JWT token that is used to access protected endpoints. Role-based authorization is used to control administrative functionality and restrict access to resources.

The application was developed incrementally by implementing the main entities and CRUD operations first, followed by authentication, authorization, relationships between entities, enrollments, notifications, and audit logging. Swagger/OpenAPI was then used to document and test the available API endpoints.

---

## User Roles

The application contains different levels of access.

### Admin

Administrators can manage the main institute resources, including:

* Users
* Students
* Instructors
* Courses
* Classes
* Enrollments
* Audit logs

### Student

Students can:

* Access student information
* View available classes
* Create enrollments
* View their own enrollments
* Delete their own enrollments
* Receive notifications

---

## User Stories

* As a user, I should be able to register and log in to the system.

* As a user, I should be able to verify my account and reset my password if needed.

* As a student, I should be able to view my student information.

* As a student, I should be able to view available classes and enroll in a class.

* As a student, I should be able to view my enrollments.

* As a student, I should be able to cancel my own enrollment.

* As an administrator, I should be able to manage users and their accounts.

* As an administrator, I should be able to create, update, and delete instructors.

* As an administrator, I should be able to create, update, and delete courses.

* As an administrator, I should be able to create, update, and delete classes.

* As an administrator, I should be able to manage student enrollments.

* As an administrator, I should be able to view audit logs to track system activities.

* As a user, I should be able to receive real-time notifications from the system.github.com/devHassan19/Institute-Management-.git



## ERD

The Entity Relationship Diagram represents the relationships between the main entities in the system.

The main entities include:

* User
* UserProfile
* Student
* Instructor
* Course
* Class
* Enrollment
* AuditLog

**ERD:**

```text
ERD.png
```

---

## Planning

The project planning was managed using GitHub to track the project's scope, deliverables, tasks, and progress.

The planning documentation includes:

* Project deliverables
* Development tasks
* Project scope
* Progress tracking
* Feature implementation

**GitHub Project / Planning:**
https://github.com/devHassan19/Institute-Management-.git
---

## API Documentation

The application provides a RESTful API documented using Swagger / OpenAPI 3.1.

Swagger UI provides interactive API documentation where developers can view available endpoints, request parameters, request bodies, responses, and test API operations.

### Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification

```text
http://localhost:8080/v3/api-docs
```

### API Endpoints

| Request Type | URL                                 | Functionality                        | Access  |
| ------------ | ----------------------------------- | ------------------------------------ | ------- |
| POST         | `/auth/users/login`                 | User login and JWT token generation  | Public  |
| POST         | `/auth/users/register`              | Register a new user                  | Public  |
| GET          | `/auth/users/verify`                | Verify user account                  | Public  |
| POST         | `/auth/users/forgot-password`       | Request password reset               | Public  |
| POST         | `/auth/users/reset-password`        | Reset user password                  | Public  |
| PUT          | `/auth/users/changePassword`        | Change password                      | Private |
| PUT          | `/auth/users/{id}/reactivate`       | Reactivate a user                    | Admin   |
| DELETE       | `/auth/users/{id}`                  | Delete a user                        | Admin   |
| GET          | `/api/students`                     | Get students                         | Private |
| GET          | `/api/students/{studentId}`         | Get student by ID                    | Private |
| PUT          | `/api/students/{studentId}`         | Update student                       | Private |
| GET          | `/api/instructors`                  | Get all instructors                  | Private |
| GET          | `/api/instructors/{instructorId}`   | Get instructor by ID                 | Private |
| POST         | `/api/instructors`                  | Create an instructor                 | Admin   |
| PUT          | `/api/instructors/{instructorId}`   | Update an instructor                 | Admin   |
| DELETE       | `/api/instructors/{instructorId}`   | Delete an instructor                 | Admin   |
| GET          | `/api/courses`                      | Get all courses                      | Private |
| GET          | `/api/courses/{courseId}`           | Get course by ID                     | Private |
| POST         | `/api/courses`                      | Create a course                      | Admin   |
| PUT          | `/api/courses/{courseId}`           | Update a course                      | Admin   |
| DELETE       | `/api/courses/{courseId}`           | Delete a course                      | Admin   |
| GET          | `/api/classes`                      | Get all classes                      | Private |
| GET          | `/api/classes/{classId}`            | Get class by ID                      | Private |
| POST         | `/api/classes`                      | Create a class                       | Admin   |
| PUT          | `/api/classes/{classId}`            | Update a class                       | Admin   |
| DELETE       | `/api/classes/{classId}`            | Delete a class                       | Admin   |
| GET          | `/api/classesSort`                  | Get sorted classes                   | Private |
| GET          | `/api/classes/pagination`           | Get paginated classes                | Private |
| GET          | `/api/enrollmrnts`                  | Get all enrollments                  | Admin   |
| POST         | `/api/enrollmrnts`                  | Create an enrollment                 | Private |
| GET          | `/api/myEnrollmrnt`                 | Get current user's enrollments       | Private |
| GET          | `/api/enrollmrnts/{enrollmentId}`   | Get enrollment by ID                 | Admin   |
| DELETE       | `/api/enrollmrnts/{enrollmentId}`   | Delete an enrollment                 | Admin   |
| DELETE       | `/api/myEnrollmrnts/{enrollmentId}` | Delete current user's enrollment     | Private |
| GET          | `/api/notifications/subscribe`      | Subscribe to real-time notifications | Private |
| GET          | `/api/auditLog`                     | Get audit logs                       | Admin   |

---

## Installation

### 1. Clone the Repository

Clone the project from GitHub:

```bash
git clone https://github.com/devHassan19/Institute-Management-.git
```

Navigate to the project directory:

```bash
cd your-repository
```

### 2. Configure PostgreSQL

Install PostgreSQL and create a database for the application.

Example:

```sql
CREATE DATABASE institute;
```

Update the database configuration in the application configuration file or environment variables.

### 3. Configure Environment Variables

Configure the required environment variables before starting the application.

Example:

```text
DB_URL=jdbc:postgresql://localhost:5432/institute
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
```

Do not commit passwords, JWT secrets, or other sensitive credentials to GitHub.

### 4. Seed the Database

Run the required database setup or seed scripts to create the initial data.

If seed data is included in the project, execute it before starting the application.

Example:

```text
database/institute.sql
```

### 5. Build the Application

Using Maven:

```bash
./mvnw clean install
```

Or:

```bash
mvn clean install
```

### 6. Start the Application

Run the Spring Boot application using:

```bash
./mvnw spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

### 7. Access the API

The REST API is available through:

```text
http://localhost:8080
```

### 8. Access Swagger

Open the following URL in a browser:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Authentication

The application uses JWT-based authentication.

Users first authenticate using:

```text
POST /auth/users/login
```

After successful authentication, the API returns a JWT token.

The token must be included when accessing protected endpoints:

```text
Authorization: Bearer <JWT_TOKEN>
```

Access to resources is controlled based on the user's role.

---

## Database

The application uses **PostgreSQL** as its relational database.

Spring Data JPA and Hibernate are used to map Java entities to database tables and manage relationships between entities.

The main relationships include:

* User → UserProfile
* User → Student
* Course → Class
* Instructor → Class
* Student → Enrollment
* Class → Enrollment

---

## Unsolved Problems

The following are known limitations or areas that may require further improvement:

* Some endpoint names contain legacy naming inconsistencies, such as `/api/enrollmrnts`.
* The current API is primarily designed as a backend service and does not include a dedicated frontend application.
* Additional validation and error handling could be added to some endpoints.
* More comprehensive automated tests could be implemented.

---

## Major Challenges

### JWT Authentication and Authorization

One of the main challenges was implementing JWT authentication together with Spring Security. The application needed to authenticate users, validate JWT tokens on protected requests, and apply role-based access control.

This was solved by implementing a JWT authentication flow using Spring Security, a JWT request filter, and user details services.

### Entity Relationships

Another challenge was designing relationships between users, students, courses, instructors, classes, and enrollments.

JPA relationships such as `@OneToOne`, `@ManyToOne`, and `@OneToMany` were used to model these relationships and maintain foreign-key relationships in PostgreSQL.

### Enrollment Access Control

The enrollment system required different access levels. Administrators can manage enrollments globally, while students should only be able to access and delete their own enrollments.

This was handled through role-based authorization and user-specific checks in the service layer.

### Real-Time Notifications

Implementing real-time notifications was another technical challenge. Server-Sent Events (SSE) were used to allow clients to subscribe to a notification stream without continuously polling the server.

### API Documentation

Swagger/OpenAPI was integrated to provide interactive documentation and make it easier to test and understand the REST API.

---

## Future Improvements

If more development time were available, the following improvements could be added:

* Build a dedicated frontend application.
* Add more comprehensive unit and integration tests.
* Improve API validation and error responses.
* Add more advanced search and filtering functionality.
* Improve notification management.
* Add email notifications for important events.
* Add instructor-specific class management.
* Add more detailed student dashboards.
* Improve pagination across all large data collections.
* Add automated CI/CD using GitHub Actions.
* Improve API security and token management.
* Add more comprehensive audit tracking.
* Add automated database migrations using Flyway or Liquibase.

---

## Project Status

The project implements the main functionality required for managing an educational institute, including authentication, authorization, students, instructors, courses, classes, enrollments, notifications, and audit logging.

The REST API is documented using Swagger/OpenAPI and can be tested through the Swagger UI after starting the application.
