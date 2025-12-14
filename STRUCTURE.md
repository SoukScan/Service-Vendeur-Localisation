# 🗂️ Structure Visuelle du Projet Vendor Service

```
vendorms/
│
├── 📁 .mvn/                          # Maven Wrapper
│
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/soukscan/vendorms/
│   │   │   │
│   │   │   ├── 📁 config/            # ⚙️ Configuration
│   │   │   │   ├── CorsConfig.java               # CORS configuration
│   │   │   │   └── RestClientConfig.java         # RestTemplate bean
│   │   │   │
│   │   │   ├── 📁 controller/        # 🎮 API Endpoints
│   │   │   │   └── VendorController.java         # 9 endpoints REST
│   │   │   │
│   │   │   ├── 📁 dto/               # 📦 Data Transfer Objects
│   │   │   │   ├── VendorRequestDTO.java         # Input validation
│   │   │   │   └── VendorResponseDTO.java        # Output format
│   │   │   │
│   │   │   ├── 📁 entity/            # 💾 Database Entity
│   │   │   │   └── Vendor.java                   # JPA Entity (14 fields)
│   │   │   │
│   │   │   ├── 📁 exception/         # ⚠️ Error Handling
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java   # Global error handler
│   │   │   │
│   │   │   ├── 📁 repository/        # 🗄️ Data Access
│   │   │   │   └── VendorRepository.java         # JPA Repository
│   │   │   │
│   │   │   ├── 📁 service/           # 💼 Business Logic
│   │   │   │   └── VendorService.java            # All business operations
│   │   │   │
│   │   │   └── 📄 VendormsApplication.java       # 🚀 Main Application
│   │   │
│   │   └── 📁 resources/
│   │       ├── application.properties            # Main config
│   │       ├── application-dev.properties        # Dev config
│   │       └── application-prod.properties       # Prod config
│   │
│   └── 📁 test/                      # 🧪 Tests
│       └── 📁 java/...
│
├── 📁 target/                        # 🔨 Build output (generated)
│
├── 📄 .env.example                   # Environment variables template
├── 📄 .gitignore                     # Git ignore rules
├── 📄 api-requests.http              # 🧪 HTTP test requests
├── 📄 build.bat                      # Windows build script
├── 📄 docker-compose.yml             # 🐳 Docker Compose
├── 📄 Dockerfile                     # 🐳 Docker image
├── 📄 mvnw                           # Maven wrapper (Unix)
├── 📄 mvnw.cmd                       # Maven wrapper (Windows)
├── 📄 pom.xml                        # 📦 Maven dependencies
├── 📄 start.bat                      # Windows start script
│
└── 📚 Documentation/
    ├── COMPLETION_SUMMARY.md         # ✅ Project completion summary
    ├── DATABASE_SETUP.md             # 💾 Database setup guide
    ├── PROJECT_OVERVIEW.md           # 📊 Architecture & overview
    ├── QUICKSTART.md                 # 🚀 Quick start guide
    ├── README.md                     # 📖 Main documentation
    ├── STRUCTURE.md                  # 🗂️ This file
    └── TESTING_GUIDE.md              # 🧪 Testing guide
```

---

## 📊 Flow de Données (Request → Response)

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │ HTTP Request
       │ POST /api/vendors
       ▼
┌────────────────────────────┐
│    VendorController        │  ← 🎮 Entry point
│  @RestController            │
│  - createVendor()           │
│  - getAllVendors()          │
│  - getVendorById()          │
└──────────┬─────────────────┘
           │ Calls service
           ▼
┌────────────────────────────┐
│     VendorService          │  ← 💼 Business logic
│  @Service                   │
│  - Validation               │
│  - Business rules           │
│  - Duplicate check          │
└──────────┬─────────────────┘
           │ Uses repository
           ▼
┌────────────────────────────┐
│   VendorRepository         │  ← 🗄️ Data access
│  @Repository                │
│  extends JpaRepository      │
│  - findByEmail()            │
│  - findByCity()             │
└──────────┬─────────────────┘
           │ JPA/Hibernate
           ▼
┌────────────────────────────┐
│   PostgreSQL Database      │  ← 💾 Storage
│   (Neon Cloud)             │
│   - Table: vendors          │
│   - 14 columns              │
└────────────────────────────┘
```

---

## 🔄 Architecture en Couches

```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                  │
│  ┌─────────────────────────────────────────────┐    │
│  │         VendorController.java               │    │
│  │  - @RestController                          │    │
│  │  - @RequestMapping("/api/vendors")          │    │
│  │  - Handle HTTP requests/responses           │    │
│  └─────────────────────────────────────────────┘    │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                   Business Layer                     │
│  ┌─────────────────────────────────────────────┐    │
│  │           VendorService.java                │    │
│  │  - @Service                                 │    │
│  │  - Business logic                           │    │
│  │  - Validation                               │    │
│  │  - Transaction management                   │    │
│  └─────────────────────────────────────────────┘    │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                 Persistence Layer                    │
│  ┌─────────────────────────────────────────────┐    │
│  │        VendorRepository.java                │    │
│  │  - @Repository                              │    │
│  │  - extends JpaRepository                    │    │
│  │  - Custom queries                           │    │
│  └─────────────────────────────────────────────┘    │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                   Database Layer                     │
│  ┌─────────────────────────────────────────────┐    │
│  │           PostgreSQL / Neon                 │    │
│  │  - Table: vendors                           │    │
│  │  - Constraints & Indexes                    │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

---

## 🧩 Entité Vendor - Schéma de Données

```
┌────────────────────────────────────────────────┐
│              TABLE: vendors                     │
├────────────────┬──────────────┬────────────────┤
│     Column     │     Type     │  Constraints   │
├────────────────┼──────────────┼────────────────┤
│ id             │ BIGSERIAL    │ PRIMARY KEY    │
│ name           │ VARCHAR(255) │ NOT NULL       │
│ description    │ VARCHAR(1000)│                │
│ email          │ VARCHAR(255) │ NOT NULL,UNIQUE│
│ phone          │ VARCHAR(255) │                │
│ address        │ VARCHAR(255) │                │
│ city           │ VARCHAR(255) │                │
│ country        │ VARCHAR(255) │                │
│ postal_code    │ VARCHAR(255) │                │
│ tax_id         │ VARCHAR(255) │                │
│ is_active      │ BOOLEAN      │ DEFAULT true   │
│ rating         │ DOUBLE       │                │
│ created_at     │ TIMESTAMP    │ AUTO           │
│ updated_at     │ TIMESTAMP    │ AUTO           │
└────────────────┴──────────────┴────────────────┘

Indexes:
  - PRIMARY KEY on id
  - UNIQUE INDEX on email
  - INDEX on city (recommended)
  - INDEX on is_active (recommended)
```

---

## 🎯 API Endpoints Mapping

```
┌─────────┬────────────────────────────────┬──────────────────────┐
│ Method  │         Endpoint               │    Description       │
├─────────┼────────────────────────────────┼──────────────────────┤
│ POST    │ /api/vendors                   │ Create vendor        │
│ GET     │ /api/vendors                   │ Get all vendors      │
│ GET     │ /api/vendors/{id}              │ Get vendor by ID     │
│ GET     │ /api/vendors/active            │ Get active vendors   │
│ GET     │ /api/vendors/city/{city}       │ Get by city          │
│ GET     │ /api/vendors/search?name=x     │ Search by name       │
│ PUT     │ /api/vendors/{id}              │ Update vendor        │
│ PATCH   │ /api/vendors/{id}/toggle-status│ Toggle active status │
│ DELETE  │ /api/vendors/{id}              │ Delete vendor        │
└─────────┴────────────────────────────────┴──────────────────────┘
```

---

## 🔐 Configuration Files Hierarchy

```
application.properties (Base)
        │
        ├── application-dev.properties (Development)
        │   - More logging
        │   - Show SQL
        │   - Auto DDL update
        │
        └── application-prod.properties (Production)
            - Less logging
            - No SQL display
            - DDL validate only
```

---

## 🌐 Microservices Communication

```
┌──────────────────────┐         ┌──────────────────────┐
│   Vendor Service     │         │   Product Service    │
│    Port: 8081        │◄───────►│    Port: 8082        │
│                      │  REST   │                      │
│  - Manage vendors    │  HTTP   │  - Manage products   │
│  - vendor_db         │         │  - product_map_db    │
└──────────────────────┘         └──────────────────────┘
         │                                  │
         │                                  │
         ▼                                  ▼
┌──────────────────────┐         ┌──────────────────────┐
│  vendor_db           │         │  product_map_db      │
│  (PostgreSQL/Neon)   │         │  (PostgreSQL/Neon)   │
└──────────────────────┘         └──────────────────────┘
```

---

## 📦 Maven Dependencies Tree

```
Spring Boot 3.5.7
├── spring-boot-starter-web
│   ├── Spring MVC
│   ├── Tomcat (embedded)
│   └── Jackson (JSON)
│
├── spring-boot-starter-data-jpa
│   ├── Hibernate
│   ├── Spring Data JPA
│   └── Transaction management
│
├── spring-boot-starter-validation
│   └── Jakarta Validation
│
├── postgresql
│   └── PostgreSQL JDBC Driver
│
└── lombok
    └── Reduce boilerplate code
```

---

## 🚀 Deployment Options

```
Option 1: Local Development
┌─────────────────────────┐
│  mvnw.cmd spring-boot:run│
│  Port: 8081              │
│  Profile: dev            │
└─────────────────────────┘

Option 2: JAR Deployment
┌─────────────────────────┐
│  mvnw.cmd clean package  │
│  java -jar app.jar       │
│  Port: 8081              │
└─────────────────────────┘

Option 3: Docker
┌─────────────────────────┐
│  docker build -t vendor  │
│  docker run -p 8081:8081 │
│  Containerized          │
└─────────────────────────┘

Option 4: Docker Compose
┌─────────────────────────┐
│  docker-compose up       │
│  Multi-service setup     │
│  Network configured      │
└─────────────────────────┘
```

---

## 📚 Documentation Files Quick Reference

| File | Purpose | When to Read |
|------|---------|--------------|
| **QUICKSTART.md** | Get started fast | First time setup |
| **README.md** | API documentation | Using the API |
| **DATABASE_SETUP.md** | DB configuration | Before first run |
| **TESTING_GUIDE.md** | Test scenarios | Testing the app |
| **PROJECT_OVERVIEW.md** | Architecture | Understanding design |
| **COMPLETION_SUMMARY.md** | What's included | Project overview |
| **STRUCTURE.md** | This file | Understanding structure |

---

## 🎓 Learning Path

```
1. Read QUICKSTART.md
   └─> Get the app running

2. Explore api-requests.http
   └─> Test the endpoints

3. Read README.md
   └─> Understand the API

4. Read TESTING_GUIDE.md
   └─> Learn testing strategies

5. Read PROJECT_OVERVIEW.md
   └─> Understand architecture

6. Study the code
   └─> Learn Spring Boot patterns
```

---

**Happy Coding! 🚀**

