# Fitness Web Application

A fitness web application designed to meet the needs of a modern gym. This project targets two main user types—clients and trainers—while also providing an administrative interface for managing the system. It is built using Spring Boot, with modern Java technologies, and integrates with several external services to support payments, scheduling, and more.

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
