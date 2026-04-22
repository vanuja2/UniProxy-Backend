# UniProxy Backend System

A secure Proxy Management and Automated Payment system built using **Spring Boot**, **MySQL**, and **Docker**.

## 🚀 Key Features
- **JWT Authentication:** Secure login and token-based access control.
- **Role-Based Security:** Different access levels for `USER` and `ADMIN`.
- **Payment Integration:** Automated crypto payments via NOWPayments API and Webhooks.
- **Proxy Management:** Ability to purchase and track proxies with automated balance deduction.

## 🛠️ Tech Stack
- **Backend:** Java 17, Spring Boot 3.x, Spring Security
- **Database:** MySQL 8.0 (Dockerized)
- **Security:** JWT (JSON Web Tokens), BCrypt Password Hashing
- **Tools:** Maven, Postman, Docker Desktop

## ⚙️ How to Run
1. Start the database using Docker: `docker run --name proxy_db ...`
2. Update `application.properties` with your DB credentials.
3. Run the app: `./mvnw spring-boot:run`