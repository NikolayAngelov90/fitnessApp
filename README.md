# Fitness Web Application

A fitness web application designed to meet the needs of a modern gym. This project targets two main user types—clients and trainers—while also providing an administrative interface for managing the system. It is built using Spring Boot with modern Java technologies and integrates with external services to support payments, scheduling, and reporting.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
  - [For Clients](#for-clients)
  - [For Trainers](#for-trainers)
  - [Admin Panel](#admin-panel)
- [Technical Details](#technical-details)
- [Default Credentials](#default-credentials)
- [Local Development (without Docker)](#local-development-without-docker)
- [Running with Docker](#running-with-docker)
- [Running with docker-compose](#running-with-docker-compose)
  - [Services](#services)
  - [Prerequisites](#prerequisites)
  - [How to start](#how-to-start)
  - [How to stop and clean up](#how-to-stop-and-clean-up)
  - [Customizing configuration](#customizing-configuration)

---

## Overview

The application allows gym members (clients and trainers) to register and log in, manage subscriptions, and book group workouts. Trainers can create and manage workout sessions, set schedules and pricing, and track their earnings. The admin panel provides tools for user management, subscription settings, trainer approvals, and revenue reporting.

## Key Features

### For Clients
- **Registration and Login:**
    - Create a user profile.
    - Login using email and password.
- **Gym Subscriptions:**
    - View available subscriptions (with or without a trainer).
    - Select and pay for monthly memberships using Stripe integration.
- **Group Workouts:**
    - Browse group workout schedules.
    - Book sessions and process payments.
- **Payment History:**
    - View previous payments.
    - Access electronic invoices for membership fees and workout bookings.

### For Trainers
- **Registration and Login:**
    - Create a trainer profile.
    - Login using email and password.
- **Workout Management:**
    - Add new workouts.
    - Set session schedules, duration, and pricing.
    - View the list of enrolled clients for each session.
- **Payments:**
    - Receive and track payments.

### Admin Panel
- **User Management:** Manage client and trainer profiles.
- **Subscription Management:** Configure memberships and pricing.
- **Trainer Approval:** Approve trainer profiles.
- **Revenue Reporting:** Generate detailed revenue reports.

## Technical Details
- **Backend Framework:** Spring Boot 3.4.1 with Spring Security, Spring Data JPA, Thymeleaf, and Spring Validation
- **Database:** MySQL is used for data persistence.
- **Payment Processing:** Integration with Stripe for secure, online payment transactions.
- **Other Technologies:**
    - **libphonenumber:** For validating and formatting phone numbers.
    - **Thumbnailator:** For image processing tasks.
    - **Spring Kafka & Testcontainers:** For asynchronous messaging and integration testing.
    - **Spring Cloud OpenFeign:** To facilitate communication with external services.
- **Java Version:** 21

## Default Credentials

Upon startup, the application creates default accounts for testing:

- **Admin**  
  Email: `admin@gmail.com`  
  Password: `ADMIN123`

- **Trainer**  
  Email: `trainer@gmail.com`  
  Password: `TRAINER123`

Use these only in non-production environments.

---

## Local Development (without Docker)

Requirements:

- Java 21
- Maven 3.9+
- Local MySQL instance (if you are not using Docker)

Basic steps:

1. Start a local MySQL database (or use the one in Docker). Ensure the database name and credentials match the ones in `src/main/resources/application.properties` or `application-prod.properties` if you run with the `prod` profile. By default the project expects a database named `fitness_app` and a MySQL `root` user.

2. Build the application with Maven:

```powershell
mvn clean package
# or skip tests for a faster build
mvn clean package -DskipTests
```

3a. Run the app with Maven (development):

```powershell
mvn spring-boot:run
```

3b. Or run the fat JAR produced in `target/`:

```powershell
# Run with the default profile
java -jar target/fitnessapp-*.jar

# Or run with the 'prod' Spring profile (if you want production config)
java -Dspring.profiles.active=prod -jar target/fitnessapp-*.jar
```

4. Open the application in your browser:

```
http://localhost:8080
```

## Running with docker-compose

### Services
- `mysql` - MySQL 8.0 database (data persisted to a Docker volume)
- `kafka` - Apache Kafka broker used by the application
- `fitness-app` - The main Spring Boot application (image: `nikolayangelov90/fitnessapp`)
- `fitness-reports-svc` - Separate reports service (image: `nikolayangelov90/fitnessreportsvc`)

### Prerequisites
- Docker Desktop (or Docker Engine) installed and running
- `docker compose` (Docker Compose v2) available on your PATH
- (Optional) If you want to build the `fitness-app` image locally: Java 21 and Maven to produce the JAR

### How to start

There are two common ways to run with the provided `docker-compose.yml`:

Option A — Use prebuilt images (compose will pull them if available):

```powershell
# From repository root (where docker-compose.yml is located)
docker compose pull
docker compose up -d
```

Option B — Build the `fitness-app` image locally and then start compose

```powershell
# 1. Build the app artifact
mvn clean package -DskipTests

# 2. Build the Docker image (Dockerfile expects the JAR in target/)
docker build -t nikolayangelov90/fitnessapp .

# 3. Start the compose stack
docker compose up -d
```

Notes:
- The compose file exposes the app on host port `8080` and the reports service on `8081`.
- MySQL is exposed on host port `3307` (mapped to container `3306`).
- If `nikolayangelov90/fitnessreportsvc` is not available on Docker Hub and you don't have a local image for it, `docker compose up` will fail for that service. You can either:
  - Remove or comment out the `fitness-reports-svc` service in `docker-compose.yml` while you work locally;
  - Or obtain/build the `fitnessreportsvc` image from its source repository.

### How to stop and clean up

```powershell
# Stop the stack
docker compose down

# Remove volumes (data) as well
docker compose down -v
```

### Customizing configuration
- The compose file passes these important environment variables to the app:
  - `SPRING_PROFILES_ACTIVE` (defaults to `prod` in the compose file)
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- If you need to change the MySQL password, database name or other settings, update `docker-compose.yml` and `init-db.sql` accordingly.

---

## Access & Default Credentials

- Admin: `admin@gmail.com` / `ADMIN123`
- Trainer: `trainer@gmail.com` / `TRAINER123`

