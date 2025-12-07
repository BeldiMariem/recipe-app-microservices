



# 🍽️ **Recipe App - Microservices Backend**

## 📋 Project Overview

This backend microservices project manages recipes, pantries, and AI-powered recipe generation.  

Recently, all services have been upgraded to **Spring Boot 4** to take advantage of the latest features and full support for **Java 21**. Previously, service discovery relied on **Eureka Server**, but to follow modern cloud-native practices, we've migrated to **Kubernetes** for orchestration. This change allows services to scale automatically, simplifies deployment, and ensures better resilience and observability. These updates make the system more maintainable, performant, and aligned with current industry standards for microservices architecture.


## 🏗️ Architecture Diagram (Kubernetes-based)

```

┌──────────────────────────────────────────────────────────────┐
│                    Client Requests                           │
└──────────────────────────────┬───────────────────────────────┘
│
┌──────────────────────────────▼───────────────────────────────┐
│                    API Gateway (Port: 8080)                  │
└─────┬────────────────┬────────────────┬────────────────┬─────┘
      │                │                │                │
┌─────▼─────┐    ┌─────▼───────┐  ┌─────▼───────┐  ┌─────▼──────┐
│ User      │    │ Pantry      │  │ AI Chef     │  │ Recipe     │
│ Service   │    │ Service     │  │ Service     │  │ Service    │
│(Port:8081)│    │ (Port:8082) │  │ (Port:8084) │  │(Port:8083) │
└───────────┘    └─────────────┘  └─────────────┘  └────────────┘
│                │                │                │
┌─────▼─────┐   ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
│PostgreSQL │   │ PostgreSQL  │  │ Google      │  │ PostgreSQL  │
│(Port:5432)│   │ (Port:5433) │  │ Gemini AI   │  │ (Port:5434) │
└───────────┘   └─────────────┘  └─────────────┘  └────────────┘
      │                │                │                │
┌─────▼────────────────▼────────────────▼────────────────▼──────┐
│                    Monitoring Stack                           │
│             Prometheus (9090) + Grafana (3000)                │
└───────────────────────────────────────────────────────────────┘

````

## 🚀 Technology Stack

### **Backend Microservices:**

* **Java 21**
* **Spring Boot 4** for all services
* **Spring Security** with JWT authentication
* **Spring Data JPA** for database operations
* **Spring Cloud Gateway** for API routing
* **Kubernetes** for modern service orchestration (replacing Eureka)
* **PostgreSQL** for data persistence
* **Google Gemini AI** for recipe generation
* **Docker** for containerization
* **GitHub Actions** for CI/CD pipeline – automates building, testing, Docker image creation, and deployment to Kubernetes

### **Monitoring & Observability:**

* **Prometheus** for metrics collection
* **Grafana** for visualization dashboards
* **Spring Boot Actuator** for health checks and metrics

### **Testing & Quality:**

* **JUnit 5** for unit testing
* **Mockito** for mocking
* **Spring Boot Test** for controller testing
---

## 📦 Microservices Breakdown

### **Core Infrastructure Services**

#### **1. API Gateway** (`api-gateway` | Port: `8080`)

Centralized entry point handling request routing, JWT validation, rate limiting, and request logging.

#### **2. Monitoring Stack** (`prometheus` + `grafana`)

* **Prometheus** (Port: `9090`): Metrics collection and storage
* **Grafana** (Port: `3000`): Visualization dashboards and alerts

### **Business Domain Services**

#### **3. User Service** (`user-service` | Port: `8081`)

Authentication and authorization service providing user registration/login, JWT token management, and role-based access control with **Spring Security 6**.

**Database:** PostgreSQL (`userdb`)

#### **4. Pantry Service** (`pantry-service` | Port: `8082`)

Inventory management system for tracking pantry items with expiration dates, quantity management, and smart organization features.

**Database:** PostgreSQL (`pantrydb`)

#### **5. Recipe Service** (`recipe-service` | Port: `8083`)

Recipe persistence layer for saving, rating, and organizing recipes.

**Database:** PostgreSQL (`recipedb`)

#### **6. AI Chef Service** (`ai-chef-service` | Port: `8084`)

Intelligent recipe generator using **Google Gemini AI** (Gemini 2.0 Flash) to create personalized recipes from available pantry ingredients.

**Features:**

* AI-powered recipe generation with multiple model support
* Fallback to rule-based recipes when AI fails
* Customizable preferences (meal type, difficulty, time)
* Structured prompt engineering for consistent recipe formatting

**AI Integration:** Handles Google Gemini API calls with advanced prompt engineering.


## 🎯 Key Technical Achievements

✅ **Upgraded all microservices to Spring Boot 4**  
✅ **Migrated service discovery from Eureka to Kubernetes** for modern cloud-native orchestration  
✅ **Complete Microservices Architecture** with health probes and readiness checks  
✅ **JWT-based Security** with API Gateway validation  
✅ **AI Integration** using Google Gemini for intelligent recipe generation  
✅ **Containerized Deployment** with Docker & Kubernetes  
✅ **Comprehensive Testing** including unit and controller tests  
✅ **Monitoring Stack** with Prometheus and Grafana  

## ⚙️ CI/CD Pipeline

To streamline development and deployment, this project includes a **full CI/CD pipeline using GitHub Actions**. Every time changes are pushed or a pull request is opened on the `main` branch, the pipeline automatically:

**1. Checks out the code** for each microservice.

**2. Builds and tests** each service using **Java 21** and **Maven**.

**3. Builds Docker images** for all services and pushes them to **Docker Hub**.

**4.** Optionally, it can deploy to **Kubernetes**, ensuring the latest version of each service is running in a cloud-native environment.

## 🚀 Quick Start Guide

### **Prerequisites:**

* Docker & Kubernetes (`kubectl` & `minikube` or a cluster)
* Google Gemini API Key

### **1. Clone and Setup:**

```bash
git clone https://github.com/BeldiMariem/recipe-app-microservices.git
cd recipe-app-microservices

# Create environment file
cp .env.example .env
# Edit .env with your Gemini API key
````

### **2. Apply Kubernetes Manifests:**

```bash
kubectl apply -f k8s-manifests/
kubectl get pods
```

### **3. Access Services:**

* API Gateway: `http://localhost:8080`
* Prometheus: `http://localhost:9090`
* Grafana: `http://localhost:3000`

---

### 🔧 **Project Structure:**

```
recipe-app-microservices/
├── api-gateway/         # API Gateway service
├── user-service/        # Authentication service
├── pantry-service/      # Pantry management service
├── ai-chef-service/     # AI recipe generation service
├── recipe-service/      # Recipe storage service
├── k8s-manifests/       # Kubernetes deployment & service YAML files
├── prometheus.yml       # Monitoring configuration
├── .env                 # Environment template
└── README.md            # Documentation
```



