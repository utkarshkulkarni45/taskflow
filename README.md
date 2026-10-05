# 🚀 Taskflow - Full-Stack Task & Project Management REST API

> A production-ready, full-stack task management application featuring a robust Spring Boot backend, relational MySQL database persistence, and a responsive Tailwind CSS frontend dashboard.

---

## ✨ Features

- **Robust REST API**: Built with Spring Boot 3 & Java, following industry-standard layered architecture (Controller, Service, Repository, DTO, Mapper).
- **Relational Data Management**: One-to-Many relationship mapping between `Projects` and `Tasks` using Spring Data JPA and Hibernate.
- **Data Validation & Integrity**: Secured with Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`) to ensure incoming requests are clean before hitting the database.
- **DTO Pattern**: Decouples database entities from API request/response contracts, preventing infinite recursion issues during JSON serialization.
- **Responsive Dashboard**: A lightweight, single-page UI built with Tailwind CSS and vanilla JavaScript supporting real-time project/task state loading.

---

## 🛠️ Tech Stack

- **Backend**: Java 25, Spring Boot 4.1.1, Spring Data JPA, Spring Security, Jakarta Validation
- **Database**: MySQL 8.0, HikariCP Connection Pooling
- **Frontend**: HTML5, Tailwind CSS, Vanilla JavaScript
- **Build Tool**: Maven

---

## 📁 Project Architecture

```text
com.example.taskflow
│
├── config/        # CORS configuration and Security filter chains
├── controller/    # REST endpoints (ProjectController, TaskController)
├── dto/           # Request and Response record DTOs
├── mapper/        # Entity-to-DTO and DTO-to-Entity mappers
├── model/         # JPA Entities (Project, Task, User)
└── repository/    # Spring Data JPA Repositories