# 🤖 AI Smart Interview Assistant

An AI-powered backend system that helps users prepare for job interviews by analyzing resumes, generating intelligent interview questions, and providing feedback on answers.

---

## 🚀 Features

* 🔐 User Authentication (JWT-based)
* 📄 Resume Upload & Parsing (PDF)
* 🤖 AI-generated Interview Questions
* 🎤 Answer Evaluation System
* 📊 Performance Feedback Dashboard
* 🧠 AI Integration (OpenAI / Custom Model)

---

## 🏗️ Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

### Database

* MySQL

### AI Integration

* OpenAI API / Python Microservice

### Tools

* Maven
* IntelliJ IDEA
* Postman

---

## 📁 Project Structure

com.ai.interview
│
├── config        → Security & Bean Configuration
├── controller    → REST APIs
├── service       → Business Logic
├── repository    → Database Access
├── entity        → Database Models
├── dto           → Data Transfer Objects
├── security      → JWT & Authentication
└── util          → Utility Classes

---

## 🔐 Authentication Flow (JWT)

1. User registers with email & password
2. Password is encrypted using BCrypt
3. User logs in with credentials
4. Server validates credentials
5. JWT token is generated
6. Token is used to access secured APIs

---

## 📌 API Endpoints

### 🔑 Auth APIs

| Method | Endpoint  | Description       |
| ------ | --------- | ----------------- |
| POST   | /register | Register new user |
| POST   | /login    | Login & get token |

---

## 🧠 Core Concepts Covered

* ORM (JPA + Hibernate)
* DTO Design Pattern
* REST API Development
* JWT Authentication
* Password Encryption (BCrypt)
* Exception Handling (Upcoming)
* Microservices Integration (AI)

---

## ⚙️ Setup Instructions

### 1. Clone the Repository

git clone https://github.com/your-username/ai-interview-assistant.git

### 2. Configure Database

Update `application.properties`:

spring.datasource.url=jdbc:mysql://localhost:3306/interview_ai
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update

---

### 3. Run the Application

Run the main class in IntelliJ

---

### 4. Test APIs

Use Postman:

* Register → /register
* Login → /login
* Use token in headers:
  Authorization: Bearer <your_token>

---

## 🔥 Future Enhancements

* 📄 Resume Parsing using Apache PDFBox
* 🤖 AI Question Generation
* 🎤 AI Answer Evaluation
* 📊 Analytics Dashboard
* 🌐 Frontend (React)
* 🐳 Docker Deployment
* ☁️ AWS Deployment

---

## 💼 Resume Value

This project demonstrates:

* Strong Backend Development (Spring Boot)
* Secure API Design (JWT Authentication)
* Real-world AI Integration
* Clean Architecture & Best Practices

---

## 👨‍💻 Author

Rohit Dhondage

---

## ⭐ If you like this project, give it a star on GitHub!
