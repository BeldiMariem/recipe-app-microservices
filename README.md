

# 🍽️ **Recipe App - Microservices Backend**

## 📋 Project Overview

A backend microservices architecture for a recipe management application. This system helps users manage their pantry and generate AI-powered recipes based on available ingredients.

## 🏗️ Architecture Diagram

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
└───────────┘   └─────────────┘  └─────────────┘  └─────────────┘
      │                │                │                │
┌─────▼────────────────▼────────────────▼────────────────▼──────┐
│                    Monitoring Stack                           │
│             Prometheus (9090) + Grafana (3000)                │
└───────────────────────────────────────────────────────────────┘
```

## 🚀 Technology Stack

### **Backend Microservices:**
- **Java 21**
- **Spring Boot 3.x** with Spring Cloud
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **Spring Cloud Gateway** for API routing
- **Eureka Service Discovery** for service registration
- **PostgreSQL** for data persistence
- **Google Gemini AI** for recipe generation
- **Docker & Docker Compose** for containerization

### **Monitoring & Observability:**
- **Prometheus** for metrics collection
- **Grafana** for visualization dashboards
- **Spring Boot Actuator** for health checks and metrics

### **Testing & Quality:**
- **JUnit 5** for unit testing
- **Mockito** for mocking
- **Spring Boot Test** for controller testing

## 📦 Microservices Breakdown

### **Core Infrastructure Services**

#### **1. Service Discovery** (`service-discovery` | Port: `8761`)
Eureka-based service registry enabling automatic service discovery, health monitoring, and load balancing across all microservices.

#### **2. API Gateway** (`api-gateway` | Port: `8080`)
Centralized entry point handling request routing, JWT validation, rate limiting, CORS, and request logging with Eureka integration for dynamic service discovery.

#### **3. Monitoring Stack** (`prometheus` + `grafana`)
- **Prometheus** (Port: `9090`): Metrics collection and storage
- **Grafana** (Port: `3000`): Visualization dashboards and alerts

### **Business Domain Services**

#### **4. User Service** (`user-service` | Port: `8081`)
Authentication and authorization service providing user registration/login, JWT token management, and role-based access control with Spring Security 6.

**Database:** PostgreSQL (`userdb`)

#### **5. Pantry Service** (`pantry-service` | Port: `8082`)
Inventory management system for tracking pantry items with expiration dates, quantity management, and smart organization features.

**Database:** PostgreSQL (`pantrydb`)

#### **6. Recipe Service** (`recipe-service` | Port: `8083`) 
Recipe persistence layer for saving, rating, and organizing Recipes

**Database:** PostgreSQL (`recipedb`)

#### **7. AI Chef Service** (`ai-chef-service` | Port: `8084`)
Intelligent recipe generator using **Google Gemini AI** (Gemini 2.0 Flash) to create personalized recipes from available pantry ingredients.

**Features:**
- AI-powered recipe generation with multiple model support
- Fallback to rule-based recipes when AI fails
- Customizable preferences (meal type, difficulty, time)
- Structured prompt engineering for consistent recipe formatting

**AI Integration:** Handles Google Gemini API calls with advanced prompt engineering.

## 🎯 Key Technical Achievements

✅ **Complete Microservices Architecture** with service discovery  
✅ **JWT-based Security** with API Gateway validation  
✅ **AI Integration** using Google Gemini for intelligent recipe generation  
✅ **Containerized Deployment** with Docker Compose  
✅ **Comprehensive Testing** including unit and controller tests  
✅ **Monitoring Stack** with Prometheus and Grafana  
✅ **Health Monitoring** with Spring Boot Actuator endpoints  
✅ **Production-ready Observability** with real-time metrics  


## 🚀 Quick Start Guide

### **Prerequisites:**
- Docker & Docker Compose
- Google Gemini API Key (free from [Google AI Studio](https://makersuite.google.com/app/apikey))

### **1. Clone and Setup:**
```bash
git clone https://github.com/BeldiMariem/recipe-app-microservices.git
cd recipe-app-microservices

# Create environment file
cp .env.example .env
# Edit .env with your Gemini API key
```

### **2. Start Services:**
```bash
# Start all services
docker-compose up -d

# Verify services are running
docker-compose ps

```


## 🔧 **Project Structure:**

```
recipe-app-microservices/
├── service-discovery/   # Eureka service registry
├── api-gateway/         # API Gateway service
├── user-service/        # Authentication service
├── pantry-service/      # Pantry management service
├── ai-chef-service/     # AI recipe generation service
├── recipe-service/      # Recipe storage service
├── prometheus.yml       # Monitoring configuration
├── docker-compose.yml   # Docker orchestration
├── .env                 # Environment template
└── README.md            # Documentation
```


