# Fitness Web Application

A fitness web application designed to meet the needs of a modern gym. This project targets two main user types—clients and trainers—while also providing an administrative interface.

## Overview

The application allows gym members and trainers to register, manage subscriptions, book workouts, and track payments.

## Key Features

### For Clients
- **Registration and Login:**
    - Create a user profile.
    - Login using email and password.
- **Gym Subscriptions:**
    - View available subscriptions.
    - Select and pay for monthly memberships.
- **Group Workouts:**
    - Browse workout schedules.
    - Book sessions and process payments.
- **Payment History:**
    - View previous payments.
    - Access electronic invoices.

### For Trainers
- **Registration and Login:**
    - Create a trainer profile.
    - Login using email and password.
- **Workout Management:**
    - Add new workouts.
    - Set session schedules, duration, and pricing.
    - View enrolled clients.
- **Payments:**
    - Receive and track payments.

### Admin Panel
- **User Management:** Manage client and trainer profiles.
- **Subscription Management:** Configure memberships and pricing.
- **Trainer Approval:** Approve trainer profiles.
- **Revenue Reporting:** Generate detailed revenue reports.

## Technical Details
- **Backend Framework:** Spring Boot 3.4.1
- **Database:** MySQL
- **Payment Processing:** Stripe
- **Other Technologies:** libphonenumber, Thumbnailator, Spring Kafka, etc.
