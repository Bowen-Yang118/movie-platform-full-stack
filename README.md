# 🎬 Fabflix Movie Platform

A scalable and cloud-native movie browsing and shopping platform with search, reviews, and purchase features — deployed using Kubernetes and AWS.
![image](https://github.com/user-attachments/assets/1d4c28f6-a14f-4fd9-b6df-3da9a61ea84c)

---

## 📚 Overview

**Fabflix** is a movie streaming and purchasing web platform inspired by real-world services like Netflix or Amazon Prime Video. It features:

- 🔍 Advanced movie search (genre, title, fuzzy match)
- 🛒 Shopping cart & checkout system
- 🧑 User authentication with JWT
- 📊 Logging, monitoring, and performance tuning
- ☁️ Cloud-native deployment using Kubernetes, Docker, AWS

---

## 🧱 Architecture

### ⚙️ Backend:
- **Java Servlets** deployed on **Tomcat**
- **MySQL** with master-slave replication
- **JDBC** for database interaction
- **Stored Procedures** and **UDFs**
- **Full-text Search**, **Elasticsearch** (optional)

### ☁️ Deployment:
- **Dockerized microservices**
- **Kubernetes** cluster with sticky sessions
- **AWS EC2**, **S3** buckets for storage
- **Fabflix Ingress** for load balancing
- **Horizontal Pod Autoscaling**

### 🔐 Security:
- HTTPS
- reCAPTCHA
- JWT Authentication
- SQL Injection protection

### 📦 Frontend:
- HTML/CSS/JavaScript with AJAX
- Mobile and web browser support
- Dynamic UI with search, genre browsing, and cart

---

## 🧪 Performance Tuning

- Connection pooling
- Caching with JDBC/MySQL
- Load balancing
- JMeter stress tests

---

## 📦 Features

- 👥 Login, Session Management
- 🔍 Movie Search with Sorting
- ⭐ Star and Movie Pages
- 🛒 Shopping Cart, Checkout, Confirmation
- 📈 Real-time data visualization (via logs/metrics)

---

## 🖥️ Demo (coming soon)

Stay tuned for a live demo and deployment instructions!

---

## 📁 Technologies

| Category       | Stack                                  |
|----------------|---------------------------------------|
| Backend        | Java, JDBC, Tomcat, MySQL             |
| Frontend       | HTML, CSS, JavaScript, AJAX           |
| Cloud          | AWS (EC2, S3), GCP                    |
| Containerization | Docker, Kubernetes                    |
| Security       | JWT, HTTPS, reCAPTCHA                 |
| Dev Tools      | JMeter, Maven, GitHub, kOps, kubectl  |

---

## 👨‍💻 Authors

Group Project — 11/2024-3/2025

---

## 🧠 Future Improvements

- User reviews & star ratings
- Movie recommendations
- Payment gateway integration
- CI/CD pipeline setup

