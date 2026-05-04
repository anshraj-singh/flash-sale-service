# ⚡ flash-sale-service
<div align="center">

[![Java Version](https://img.shields.io/badge/Java-17-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.X-brightgreen.svg)]()
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)]()
[![Redis](https://img.shields.io/badge/Redis-Locking-red.svg)]()
[![Kafka](https://img.shields.io/badge/Kafka-Streaming-black.svg)]()

</div>

**flash-sale-service** is a high-performance, distributed backend engine architected to solve critical data consistency and system availability problems during extreme traffic spikes (1k+ Req/Sec).

---

## 💡 The Problem & Solution

### The Core Challenge: Resource Concurrency
In high-traffic distributed systems, when thousands of users attempt to access or decrement a limited resource (like a specific inventory item, a limited-time offer, or a booking slot) at the exact same millisecond, traditional relational databases often reach their limits.

**The critical issues include:**
* **Race Conditions:** Simultaneous requests reading the same initial state and attempting to update it, leading to inconsistent data.
* **Over-allocation:** A scenario where the system sells more units than are actually available (e.g., selling 105 items when stock is only 100).
* **Database Bottlenecks:** Row-level locking in SQL databases creates massive latency, often leading to system timeouts or crashes during extreme spikes.

### The flash-sale-service Solution (Architecture)
This service solves these challenges by **decoupling real-time validation from persistent storage**.

1. **Atomic Distributed Locking (Redis):** Instead of querying the slower MySQL database for every transaction, the system utilizes Redis as a high-speed "Resource Guard." By using atomic `DECR` operations, it ensures that only the authorized number of requests pass through, instantly preventing over-selling in-memory.
2. **Event-Driven Asynchronous Processing (Kafka):** Validated requests are immediately pushed to an **Apache Kafka** cluster. This allows the API to respond to the user in milliseconds, while a dedicated consumer service processes the actual order persistence in the background at a controlled pace.

This architecture guarantees **100% data consistency** and high availability even during the most intense traffic bursts.

---

## 🛠️ Key Architectural Features

* ✅ **Role-Based Access Control (RBAC):** Distinct roles for Customers, Vendors, and Admins secured by **Spring Security 6 & JWT**.
* ✅ **Distributed Stock Management:** Real-time, atomic inventory updates using **Redis** to prevent over-selling.
* ✅ **Event-Driven Processing:** High-concurrency orders are queued and processed asynchronously via **Apache Kafka**.
* ✅ **Production-Ready Entities:** Structured Database schemas for Users, Products, Sales, and Orders.
* ✅ **Dockerized Environment:** Easily portable and scalable containers for the App, Redis, Kafka, and MySQL.

---

## 🚀 Module 1 Completion: Security & Identity

The foundational identity and security layer is now operational. This module establishes a secure environment for all subsequent high-concurrency operations.

* **Identity Management:** Secure onboarding for Admin, Vendor, and Customer roles.
* **Password Security:** Industry-standard **BCrypt** hashing.
* **Stateless Auth:** **JWT-based** authentication with a custom security filter.
* **Performance Focused:** Built with constructor injection for optimal bean management.

### ✅ Finalized API Endpoints (Module 1)

#### 1. Identity Registration (Public)
`POST /api/v1/users/register`
* **Goal:** Onboard users with encrypted credentials and specific roles.
Used by new Customers and Vendors to join the platform.

**Request JSON:**
```json
{
  "username": "anshik_vendor",
  "email": "vendor@flashrush.com",
  "password": "strongpassword123",
  "role": "VENDOR"
}
```

Response (Success):
User registered successfully!

#### 2. User Login (Public Access)
`POST /api/v1/users/login`

Used to authenticate and receive a secure stateless JWT token for future requests.

**Request JSON:**

```json
{
  "username": "anshik_vendor",
  "password": "strongpassword123"
}
```
Response DTO (JSON):

```json
{
    "username": "anshik_vendor",
    "role": "VENDOR",
    "type": "Bearer",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## 🚀 Module 2 Completion: Resource Management & Stock Warm-up

In this module, we transitioned from basic identity management to **high-concurrency resource handling**. We implemented the logic to bridge our persistent storage (MySQL) with our high-speed distributed cache (Redis Cloud).

### Key Technical Achievements:
* **Product Inventory System:** Developed a robust product management layer where Vendors can register goods.
* **Flash Sale Scheduling:** Implemented a scheduling engine to define sale windows (Start/End time) and stock limits.
* **The "Stock Warm-up" Pattern:** Architected a proactive caching mechanism. Upon sale creation, the inventory is instantly pushed to **Redis Cloud** to avoid database bottlenecks during the sale.
* **Distributed Consistency:** Integrated **Jedis** with **SSL/TLS** for secure, high-speed communication with managed Redis instances.
* **Automated TTL (Time To Live):** Implemented a 24-hour expiration on Redis keys to ensure efficient memory management on the cloud.

---

1. **Vendor Action:** Vendor schedules a sale for 500 units.
2. **Persistence:** Data is saved in **MySQL** (The Permanent Truth).
3. **Warm-up:** Backend triggers a `SET` command to **Redis Cloud** with a key like `flash_sale_stock:{saleId}`.
4. **Readiness:** The system is now ready to handle 10k+ requests per second without touching the MySQL database for stock checks.

---

## ✅ Finalized API Endpoints (Module 2)

### 1. Add New Product
`POST /api/v1/vendor/products`
* **Access:** Vendor Only (Secured via JWT)
* **Goal:** Register a product into the permanent catalog.

**Request Body:**
```json
{
  "name": "Sony PlayStation 5",
  "description": "Next-gen gaming console - Digital Edition",
  "price": 45000.0
}
```

### 2. Create & Warm-up Flash Sale
`POST /api/v1/vendor/flash-sale/create`
* **Access:** Vendor Only (Secured via JWT)
* **Goal:** Schedule a sale and pre-load stock into Redis Cloud.

**Request Body:**
```json
{
  "productId": 5,
  "salePrice": 35000.0,
  "startTime": "2026-05-10T10:00:00",
  "endTime": "2026-06-10T12:00:00",
  "quantity": 500
}
```

### 🔒 Data Isolation & Vendor Security
To ensure business privacy, the system implements strict data isolation. Vendors can only access and manage their own flash sales and product catalogs.

#### Fetch My Flash Sales (Private)
`GET /api/v1/vendor/flash-sale/my-sales`
* **Access:** Vendor Only (Authenticated via JWT)
* **Logic:** Filters the database records based on the logged-in `username` extracted from the security context.

**Response Body (Filtered for Logged-in Vendor):**
```json
[
  {
    "id": 24,
    "product": {
      "id": 5,
      "name": "Sony PlayStation 5",
      "price": 45000.0
    },
    "salePrice": 35000.0,
    "startTime": "2026-05-10T10:00:00",
    "endTime": "2026-06-10T12:00:00",
    "quantity": 500
  }
]
```

---

## 🏗️ Module 3: Distributed Ordering Engine (Apache Kafka & Redis)

In this module, the project evolves into a **High-Concurrency Event-Driven System**. We moved away from synchronous database writes to an asynchronous architecture to handle traffic spikes during a flash sale.

### 🌟 Technical Highlights
* **Asynchronous Processing:** Integrated **Confluent Kafka Cloud** to decouple order placement from database persistence.
* **Atomic Inventory Management:** Used **Redis Atomic Decrement (`DECR`)** to ensure exact stock counts and prevent over-selling under heavy load.
* **Cloud-Native Messaging:** Fully integrated with **Confluent Cloud** using SASL/SSL for secure, managed message streaming.
* **Event-Driven Consistency:** Implemented a Producer-Consumer pattern where the consumer ensures eventual consistency by persisting orders to MySQL.

---

### 🔄 The "Buy Now" Workflow

1. **Client Request:** A customer hits the `/buy` endpoint.
2. **Redis Check:** The system performs a high-speed atomic stock check in **Redis Cloud**.
3. **Kafka Producer:** If stock is available, an `OrderEvent` (Status: `PENDING`) is published to the `flash-sale-orders` topic on Confluent Cloud.
4. **Immediate Ack:** The customer receives a `202 Accepted` response with a unique Tracking ID.
5. **Kafka Consumer:** The background listener picks up the event, creates a record in **MySQL**, and updates the status to `SUCCESS`.

---

### 📡 API Documentation

#### **1. Place Order (High-Speed Path)**
`POST /api/v1/orders/buy`
* **Access:** `ROLE_CUSTOMER`
* **Flow:** Redis `DECR` → Kafka `PUBLISH` → Instant Response.

**Request Payload:**
```json
{
  "saleId": 25,
  "productId": 6,
  "quantity": 1
}
```

**Response (202 Accepted):**
```json
{
  "message": "Order Accepted! Tracking ID: 8f2a1b-9c4d-4e5f-a123-bc4455667788"
}
```

### 📦 Event Structure (Kafka JSON)
This is the payload that travels through the Confluent Cloud Pipeline:

```json
{
"orderId": "8f2a1b-9c4d-4e5f-a123-bc4455667788",
"username": "anshraj_customer",
"productId": 6,
"saleId": 25,
"price": 45000.0,
"status": "PENDING"
}
```

<img width="1919" height="944" alt="Screenshot 2026-04-26 231007" src="https://github.com/user-attachments/assets/19a67906-2fa9-4c87-97f1-89bebfb9a3a4" />


---

## Module 4: System Optimization & Resilience (Idempotency & Caching)

In this core development, the system was optimized for high reliability and extreme performance. We addressed real-world challenges like duplicate requests (Idempotency) and database bottlenecking (Caching).

### 🛠️ Key Improvements
* **Request Idempotency:** Implemented a mechanism to prevent duplicate orders if a user clicks the "Buy" button multiple times.
* **Distributed Caching:** Integrated **Redis Cache** to offload read pressure from MySQL for frequently accessed data like order history.
* **Fault Tolerance:** Demonstrated Kafka's ability to persist messages even when the consumer is offline, ensuring **Eventual Consistency**.
* **Performance Boost:** Reduced latency for order history retrieval from seconds to milliseconds.

---

### 🛡️ Idempotency Logic
To prevent race conditions and duplicate billing, we implemented a **Unique Request-ID** check using Redis.

1. **Client Side:** Every "Buy" request must include a unique `X-Request-ID` in the header.
2. **Server Side:** Spring Boot uses Redis `SETNX` (Set if Not Exists) to lock that ID for 10 minutes.
3. **Outcome:** If the same ID is sent again, the system returns a `409 Conflict` instead of processing a new order.



---

### ⚡ Read-Through Caching Pattern
To optimize the `/my-orders` endpoint, we implemented the **Cache-Aside** pattern:
* **Cache Hit:** If orders exist in Redis, return them instantly.
* **Cache Miss:** If not in Redis, fetch from MySQL, store in Redis, and then return.

**Result:** Database load reduced by ~90% for repeated "Check Order" actions.

---

### API Documentation

#### **1. Secure & Idempotent Order Placement**
`POST /api/v1/orders/buy`
* **Access:** `ROLE_CUSTOMER`
* **Mandatory Header:** `X-Request-ID` (UUID)

**Request Payload:**
```json
{
  "saleId": 26,
  "productId": 7,
  "quantity": 2
}
```

**Response (409 Conflict - For Duplicates):**

```json
{
  "status": 409,
  "message": "Duplicate Request! This order is already being processed.",
  "timestamp": "2026-04-25T10:30:00"
}
```

#### **2.  Cached Order History**
`GET /api/v1/orders/my-orders`

* **Access:** `ROLE_CUSTOMER`

Optimization: Redis Caching Enabled.

**Response Body:**

```json
[
  {
    "id": "a0ae0720-33cd-430f-8042-56d443f469ba",
    "username": "anshraj",
    "productId": 7,
    "totalAmount": 30000.0,
    "status": "SUCCESS",
    "createdAt": "2026-04-25T11:01:13"
  }
]
```

<img width="1349" height="812" alt="Screenshot 2026-04-25 235320" src="https://github.com/user-attachments/assets/4743b098-0e94-4ab7-8287-5b5ad85c456d" />


---

## Module 5: API Governance, Validation & Async Communications

transforms the system into a production-grade application by implementing professional error handling, strict input validation, and asynchronous user communication (Email Notifications).

### 🚀 Key Features
* **Global Exception Handling:** Centralized mechanism to catch and format all application errors into a standard JSON structure.
* **Strict Request Validation:** Using JSR-303 Bean Validation to ensure data integrity (e.g., preventing negative quantities or null fields).
* **Asynchronous Emailing:** Integrated **Spring Boot Mail** with `@Async` to send order confirmations without blocking the main execution thread.
* **Professional API Responses:** Replaced default Spring error pages with clear, actionable error messages.

---

### 📋 API Validation & Integrity
Every incoming request is now validated at the Controller level. If the data doesn't meet the criteria, the system rejects it before it even touches the Database or Kafka.

**Validation Rules:**
- `saleId` & `productId`: Cannot be Null.
- `quantity`: Minimum value is 1.



---

### Asynchronous Notification System
To maintain the high-speed nature of a Flash Sale, the email service operates in the background:
1. **Order Consumer** saves the event to MySQL.
2. **Email Service** is triggered asynchronously using a background thread pool.
3. **Outcome:** The user's order is processed in milliseconds, and the email follows shortly after.

---

### API Documentation

#### **1. Standard Error Response Format**
Whenever an error occurs, the system returns this consistent structure:

```json
{
  "timestamp": "2026-04-26T00:05:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation Failed",
  "details": [
    "quantity: Must be at least 1",
    "saleId: Sale ID is mandatory"
  ]
}
```

#### **2. Test Case: Validation Fail**
`POST /api/v1/orders/buy`

**Request Body (Invalid):**
```json
{
   "saleId": null,
   "productId": 7,
   "quantity": 0
}
```
**Response:** 400 Bad Request with the professional JSON shown above.


#### **3. Test Case: Success with Email**

**Console Output:**

```
INFO: Order Received from Confluent Cloud: 8f2a1b...
INFO: Order saved to MySQL: 8f2a1b...
INFO: Email sent successfully to: anshraj@gmail.com
```

---

##  Advanced System Control: Rate Limiting, Dynamic Sales & Audit

In this phase, we implemented mission-critical features to protect the system from bot attacks, ensure time-sensitive execution, and verify data consistency across distributed layers.

### Key Implementations

#### **1. Distributed Rate Limiting (The Bodyguard)**
To prevent DDoS attacks and script-based bot entries, we implemented a **Fixed Window Rate Limiter** using Redis.
* **Logic:** Each user is limited to **5 requests per minute**.
* **Mechanism:** We track the request count in Redis with a 60-second TTL (Time-To-Live).
* **Outcome:** Protects the system infrastructure from being overwhelmed during peak traffic.

#### **2. Dynamic Sale Activation (The Scheduler)**
The system now automatically manages the lifecycle of a Flash Sale based on its `startTime` and `endTime`.
* **Logic:** The `OrderService` validates the current system time against the sale's database record.
* **Validation:** - If `currentTime < startTime`: Sale hasn't started (Request Rejected).
    - If `currentTime > endTime`: Sale has expired (Request Rejected).

#### **3. Stock Reconciliation & Audit System (The Accountant)**
A specialized Admin-only tool to detect data mismatch between the High-Speed Cache (Redis) and the Permanent Storage (MySQL).
* **Purpose:** Ensures **Eventual Consistency** by verifying if:
  `Redis Remaining Stock + MySQL Processed Orders == Initial Total Stock`.

---

### 📡 API Documentation (Advanced Governance)

#### **1. Rate Limit Enforcement**
`POST /api/v1/orders/buy`

**Scenario:** User sends more than 5 requests in a minute.

**Response (429 Too Many Requests / 500 Error):**
```json
{
  "timestamp": "2026-04-26T22:15:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Rate limit exceeded! Try again after a minute.",
  "details": null
}
```

#### **2. Time-Based Validation**
**Scenario:** Sale is scheduled for tomorrow but accessed today.
**Response:**

```json
{
"status": 500,
"message": "Sale has not started yet!",
"error": "Internal Server Error"
}
```

#### **3. Admin Stock Audit API**
`GET /api/v1/admin/audit/stock/{saleId}`

**Access:** ROLE_ADMIN

**Response Body:**

```json
{
"saleId": 28,
"remainingInRedis": 95,
"processedInMySQL": 5,
"systemTimestamp": "2026-04-26T22:30:00",
"isConsistent": true,
"summary": "System Audit: 5 orders processed, 95 items left in stock."
}
```

## 🐳 Docker Implementation Details

This project is fully containerized to ensure a consistent development and production environment. We use **Docker Compose** to orchestrate the microservice and its persistent storage.

### 🏗️ 1. Multi-Stage Docker Build
To keep the production image lightweight and secure, we use a **Multi-stage Build** approach in our `Dockerfile`:

* **Stage 1 (Build):** Uses `maven:3.8.4-openjdk-17-slim` to compile the source code and package it into a `.jar` file. This stage is isolated from the final image.
* **Stage 2 (Runtime):** Uses `eclipse-temurin:17-jdk-alpine`. We only copy the necessary `.jar` from the build stage. Alpine Linux is used to keep the final image size under **200MB** and reduce the attack surface.

### ⚙️ 2. Service Orchestration (Docker Compose)
The `docker-compose.yml` file manages two primary services:

1.  **`springboot-app`**:
    * **Auto-Build:** Automatically triggers the Dockerfile build process.
    * **Dependency Management:** Uses `depends_on` with `service_healthy` condition to ensure the database is ready before the app starts.
    * **Network Bridge:** Connects to Cloud Redis and Confluent Kafka via the host's internet connection.

2.  **`mysqldb` (MySQL 8.0)**:
    * **Port Mapping:** Maps container port `3306` to host port `3306` (or `3307`) for external access via MySQL Workbench.
    * **Data Persistence:** Uses **Docker Volumes** (`mysql_data:/var/lib/mysql`) so that your flash sale data is not lost when the container is stopped or removed.

### 🔐 3. Security & Environment Configuration
- **Zero-Secret Leakage:** All sensitive credentials (Redis/Kafka/DB passwords) are stored in a local `.env` file, which is excluded from the Docker image via `.dockerignore` and from GitHub via `.gitignore`.
- **Dynamic Injection:** Environment variables are injected into the container at runtime, making the image portable across different environments (Dev, QA, Prod).

### 🚀 How to Run

1.  **Stop any local MySQL service** running on your machine to avoid port conflicts.
2.  **Build and Start** the entire stack:
    ```bash
    docker-compose up --build
    ```
3.  **Monitor Logs**:
    ```bash
    docker logs -f springboot-app
    ```

### 📂 Docker-related Files in this Repo:
- `Dockerfile`: Instructions for building the Spring Boot image.
- `docker-compose.yml`: Defines services, networks, and volumes.
- `.dockerignore`: Lists files/folders to be ignored during the Docker build (e.g., `target/`, `.git/`, `.env`).

<img width="1919" height="1079" alt="Screenshot 2026-04-27 225020" src="https://github.com/user-attachments/assets/79aa3c0a-5315-437c-9f1c-855dfecf9992" />
