# LakeSide Hotel Backend API

Spring Boot backend for a hotel booking system with JWT authentication, role-based authorization, room management, and booking management.

## 1) Project Overview

This project provides REST APIs for:
- user registration and login
- role management (`ROLE_USER`, `ROLE_ADMIN`)
- room creation, update, delete, and listing
- room availability search by date range
- booking creation, lookup, and cancellation

The backend uses MySQL for persistence and Spring Security for authentication/authorization.

## 2) Tech Stack

- Java 17
- Spring Boot 3.1.4
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Security
- JWT (`jjwt`)
- MySQL
- Maven

## 3) Main Features (Simple Explanation)

### A) User Registration
- Endpoint: `POST /auth/register-user`
- User sends first name, last name, email, and password.
- System checks if email already exists.
- Password is encrypted with BCrypt.
- New user is assigned default role: `ROLE_USER`.

### B) Login + JWT
- Endpoint: `POST /auth/login`
- User sends email + password.
- If credentials are valid, system generates JWT token.
- Token includes user identity (email) and roles.
- Client uses this token in next requests:
  - `Authorization: Bearer <token>`

### C) Role Management
- Create role, list roles, assign/remove user-role mapping.
- Role format is normalized as `ROLE_<NAME>`, for example `ROLE_ADMIN`.

### D) Room Management
- Add room with photo upload.
- Update room details and photo.
- Delete room.
- List all room types.
- List all rooms.

Room photo is stored as BLOB in DB and returned as Base64 in API responses.

### E) Availability Search
- Endpoint: `GET /rooms/available-rooms`
- Input: `checkInDate`, `checkOutDate`, `roomType`
- System returns only rooms that are not already booked for overlapping dates.

### F) Booking Management
- Create booking for a room and date range.
- Auto-generates booking confirmation code.
- Fetch booking by confirmation code.
- Fetch bookings by guest email.
- Cancel booking.

## 4) Booking Concurrency Handling (Double-Booking Prevention)

This project now includes concurrency-safe booking logic.

### What is implemented
- Pessimistic write lock on room row during booking transaction.
- DB overlap check query before saving booking.
- Booking save is wrapped in a transaction.

### Why this matters
If two users try to book the same room at the same time:
- first transaction locks the room row
- second transaction waits
- after first commit, second request re-checks overlap
- if dates clash, second booking is rejected

This prevents race-condition based double booking for the same room.

## 5) Security Flow

1. User logs in with email/password.
2. Server validates credentials using Spring Security + `UserDetailsService`.
3. Server returns JWT token.
4. For protected APIs, client sends JWT in `Authorization` header.
5. JWT filter validates token and sets authenticated user in security context.
6. Authorization is applied using:
- URL-level rules in Security Filter Chain
- method-level rules using `@PreAuthorize`

## 6) API Summary

### Auth APIs
- `POST /auth/register-user`
- `POST /auth/login`

### User APIs
- `GET /users/all`
- `GET /users/{email}`
- `DELETE /users/delete/{userId}` (currently deletes by email variable)

### Role APIs
- `GET /roles/all-roles`
- `POST /roles/create-new-role`
- `DELETE /roles/delete/{roleId}`
- `POST /roles/remove-all-users-from-role/{roleId}`
- `POST /roles/remove-user-from-role`
- `POST /roles/assign-user-to-role`

### Room APIs
- `POST /rooms/add/new-room`
- `GET /rooms/room/types`
- `GET /rooms/all-rooms`
- `GET /rooms/room/{roomId}`
- `PUT /rooms/update/{roomId}`
- `DELETE /rooms/delete/room/{roomId}`
- `GET /rooms/available-rooms`

### Booking APIs
- `GET /bookings/all-bookings`
- `POST /bookings/room/{roomId}/booking`
- `GET /bookings/confirmation/{confirmationCode}`
- `GET /bookings/user/{email}/bookings`
- `DELETE /bookings/booking/{bookingId}/delete`

## 7) Database Configuration

Configured in `src/main/resources/application.properties`:
- DB: MySQL
- URL: `jdbc:mysql://localhost:3306/lakeSide_hotel_db`
- Username: `root`
- Password: `admin`
- Hibernate: `ddl-auto=update`
- Server port: `9192`

## 8) How to Run Locally

### Prerequisites
- Java 17
- MySQL running locally
- Database created: `lakeSide_hotel_db`

### Steps
1. Clone repository
2. Update DB credentials in `application.properties` if needed
3. Run:

```bash
sh mvnw spring-boot:run
```

4. API base URL:
- `http://localhost:9192`

## 9) Example Request Flow (End-to-End)

1. Register user:
- `POST /auth/register-user`

2. Login:
- `POST /auth/login`
- copy JWT from response

3. Call protected endpoint:
- add header: `Authorization: Bearer <token>`

4. Search available rooms:
- `GET /rooms/available-rooms?checkInDate=2026-03-10&checkOutDate=2026-03-12&roomType=Deluxe`

5. Book room:
- `POST /bookings/room/{roomId}/booking`

6. Save and use booking confirmation code for lookup.

## 10) Resume-Ready Highlights (Copy/Paste)

### Short Version
- Built a Spring Boot hotel booking backend with JWT authentication, role-based access control, and MySQL persistence.
- Implemented room and booking APIs with availability checks and confirmation-code based booking lookup.
- Added concurrency-safe booking using transactional flow, pessimistic DB locking, and overlap validation to prevent double booking.

### Detailed Version
- Designed and developed REST APIs for authentication, user management, role management, room inventory, and booking lifecycle operations.
- Integrated Spring Security with stateless JWT flow (login token generation, token validation filter, and role-based authorization).
- Modeled relational entities (`User`, `Role`, `Room`, `BookedRoom`) with JPA including many-to-many and one-to-many mappings.
- Implemented room photo upload/storage using multipart handling and BLOB persistence with Base64 response mapping.
- Improved booking reliability by implementing transactional, lock-based concurrency control to prevent race conditions and duplicate bookings under concurrent requests.
- Configured MySQL/Hibernate integration, schema auto-update, SQL logging, and production-style layered architecture (controller/service/repository).

## 11) Future Improvements (Optional)

- Add integration tests for parallel booking requests.
- Add global exception handler (`@ControllerAdvice`) for consistent API error responses.
- Add refresh token flow for long-lived sessions.
- Add API documentation (OpenAPI/Swagger).
- Add Docker setup for app + MySQL.

