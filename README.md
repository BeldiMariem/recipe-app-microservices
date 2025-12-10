
# 🍽️ **Recipe App - Full Stack Microservices**

## 📋 Project Overview

This is a full-stack recipe management application with a modern **Angular frontend** and a **Spring Boot microservices backend**. The system manages recipes, pantries, and AI-powered recipe generation.

### **Recent Major Upgrades:**
- ✅ **All backend services upgraded to Spring Boot 4** for latest features and full **Java 21** support
- ✅ **Migration from Eureka Server to Kubernetes** for modern cloud-native orchestration
- ✅ **Complete Angular frontend** with responsive design and modern UX patterns
- ✅ **AI Chef integration** using Google Gemini for intelligent recipe suggestions
- ✅ **Automated CI/CD Pipeline** with GitHub Actions for seamless deployment

---


## 🏗️ **Full Stack Architecture**

### **Frontend Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                    Angular Frontend                         │
│                   (Port: 4200 / Deployed)                   │
├─────────────────────────────────────────────────────────────┤
│  Components       │  Services      │  State      │  Shared  │
│  • AI Chef        │  • Auth        │  • Signals  │  • Models│
│  • Recipes        │  • Recipe      │  • RxJS     │  • Guards│
│  • Pantry         │  • Pantry      │  • Store    │  • Pipes │
│  • Dashboard      │  • AI Chef     │             │  • Utils │
└─────────────────────────────────────────────────────────────┘
```

### **Backend Architecture (Kubernetes-based)**
```
┌──────────────────────────────────────────────────────────────┐
│                    Angular Frontend                          │
│                      (Port: 4200)                            │
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
```

---

## 🚀 **Technology Stack**

### **Frontend**
- **Angular 21** with TypeScript
- **Angular Material** & **Bootstrap 5** for UI components
- **RxJS** for reactive programming
- **Angular Signals** for state management
- **Docker** for containerization

### **Backend Microservices**
- **Java 21** with **Spring Boot 4**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **Spring Cloud Gateway** for API routing
- **Kubernetes** for orchestration (replacing Eureka)
- **PostgreSQL** for data persistence
- **Google Gemini AI** for recipe generation
- **Docker** for containerization


### **DevOps & Monitoring**
- **GitHub Actions** for CI/CD
- **Prometheus** for metrics collection
- **Grafana** for visualization
- **Docker Compose** for local development
- **Kubernetes** for production deployment

---

## 🔧 **Project Structure**

```
recipe-app-fullstack/
├── 📁 frontend/                    # Angular Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   ├── ai-chef/       # 🎯 AI Chef Component
│   │   │   │   │   ├── ai-chef.component.html
│   │   │   │   │   ├── ai-chef.component.ts
│   │   │   │   │   └── ai-chef.component.scss
│   │   │   │   ├── recipes/       # Recipe management
│   │   │   │   ├── pantry/        # Pantry management
│   │   │   │   └── dashboard/     # User dashboard
│   │   │   ├── services/
│   │   │   │   ├── ai-chef.service.ts
│   │   │   │   ├── recipe.service.ts
│   │   │   │   └── auth.service.ts
|   |   |   ├── guards/            # Route guards
|   |   |   ├── interceptors/      # HTTP interceptors
│   │   │   └── models/            # TypeScript interfaces
│   │   └── assets/                # Images, styles, icons
│   ├── angular.json
│   ├── package.json
│   └── Dockerfile                 # Production Docker build
│   
├── 📁 backend/                     # Spring Boot Microservices
│   ├── api-gateway/               # API Gateway service
│   ├── user-service/              # Authentication service
│   ├── pantry-service/            # Pantry management
│   ├── ai-chef-service/           # AI recipe generation
│   └── recipe-service/            # Recipe storage
│
├── 📁 k8s-manifests/              # Kubernetes configurations
├── docker-compose.yml             # Local development setup
├── 📁 scripts/                    # Deployment scripts
├── .env.example                   # Environment variables
└── README.md                      # This file
```

---


### **Frontend-Backend Integration**
```typescript
Frontend (Angular) → API Gateway → AI Chef Service → Gemini AI
      ↓
Response with recipes ← Fallback recipes (if API fails)
```

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

## 🚀 **CI/CD Pipeline**

### **GitHub Actions Workflows**

The project includes **automated CI/CD pipelines** that run on every push and pull request:

#### **📦 Backend Services Pipeline** (`.github/workflows/backend-ci.yml`)
```yaml
✅ Builds and tests 5 Spring Boot microservices in parallel
✅ Runs Maven tests for each service
✅ Builds Docker images for all services
✅ Pushes images to Docker Hub registry
✅ Uses JDK 21 with optimized caching
```

#### **🎨 Frontend Application Pipeline** (`.github/workflows/frontend-ci.yml`)
```yaml
✅ Builds Angular 21 application with optimization
✅ Runs unit tests with Chrome Headless
✅ Caches npm dependencies for faster builds
✅ Uses Docker Buildx with multi-layer caching
✅ Pushes production-ready Docker image
✅ Smart path-based triggering (only runs when frontend changes)
```

### **Pipeline Status Badges**

[![Backend CI](https://github.com/BeldiMariem/recipe-app-microservices/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/BeldiMariem/recipe-app-microservices/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/BeldiMariem/recipe-app-microservices/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/BeldiMariem/recipe-app-microservices/actions/workflows/frontend-ci.yml)

## 🚀 **Quick Start Guide**

### **Prerequisites:**
- Node.js 18+ & npm
- Java 21
- Docker & Docker Compose
- Google Gemini API Key

### **1. Clone and Setup:**
```bash
git clone https://github.com/BeldiMariem/recipe-app-microservices.git
cd recipe-app-microservices

cp .env.example .env
# Edit .env with your API keys
```

### **2. Start Using Docker Compose:**
```bash
docker-compose up -d
```

### **4. Access the Application:**
- **Frontend**: http://localhost:4200
- **API Gateway**: http://localhost:8080
---

## 👩‍💻 Developed with ❤️ by Mariem BELDI.





