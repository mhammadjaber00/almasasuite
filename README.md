# Almasa Suite - Comprehensive Jewelry Store Management System

## 🏪 Project Overview

Almasa Suite is a complete jewelry store management system designed for modern jewelry retailers. It combines a powerful backend API with an intuitive desktop Point-of-Sale (POS) application to handle every aspect of jewelry business operations.

### What This System Does

- **Inventory Management**: Track jewelry products with detailed specifications (karat, weight, type, design fees)
- **Point of Sale**: Process sales transactions with offline capability
- **Gold Intake Management**: Record gold purchases from vendors and customers with liability tracking
- **Vendor Management**: Manage supplier relationships and payment obligations
- **Profit Tracking**: Calculate and track profit margins on sales
- **User Management**: Role-based access control (Admin, Manager, Staff)
- **Reporting**: Comprehensive business intelligence and reporting

### Why This Architecture?

This project demonstrates modern software architecture principles:

1. **Separation of Concerns**: Clear separation between server, client, and shared logic
2. **Offline-First Design**: POS system works without internet connectivity
3. **Microservices Ready**: Containerized backend ready for cloud deployment
4. **Cross-Platform**: Desktop app runs on Windows, macOS, and Linux
5. **Type Safety**: Kotlin's type system prevents runtime errors
6. **Modern UI**: Compose Multiplatform provides native performance with modern design

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   POS Desktop   │    │     Server      │    │   PostgreSQL    │
│  (Compose MP)   │◄──►│     (Ktor)      │◄──►│   Database      │
│                 │    │                 │    │                 │
│  Local SQLite   │    │  JWT Auth       │    │  Flyway         │
│  Offline Mode   │    │  REST API       │    │  Migrations     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Shared      │    │     Docker      │    │     MinIO       │
│    Models       │    │   Container     │    │ Object Storage  │
│  Repositories   │    │                 │    │ (Images)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Module Structure

#### 1. **Server Module** (`/server`)
- **Technology**: Ktor with Netty engine
- **Database**: PostgreSQL with Exposed ORM
- **Authentication**: JWT with BCrypt password hashing
- **Migrations**: Flyway for database versioning
- **Deployment**: Docker containerization

**Key Components**:
- `Application.kt`: Main server entry point with routing
- `plugins/`: Security, database configuration
- `routes/`: REST API endpoints (auth, products, sales, gold intake)
- `services/`: Business logic layer
- `models/`: Server-side data models

#### 2. **POS Desktop Module** (`/pos-desktop`)
- **Technology**: Compose Multiplatform with Material 3
- **Local Storage**: SQLite with SQLDelight
- **Networking**: Ktor client for API communication
- **Offline Support**: Full functionality without internet

**Key Components**:
- `ui/`: Compose UI screens and components
- `viewmodel/`: State management and business logic
- `service/`: API communication and data synchronization
- `repository/`: Local data access layer

#### 3. **Shared Module** (`/shared`)
- **Purpose**: Common code between server and client
- **Models**: Data classes with Kotlin serialization
- **Repositories**: Abstract data access interfaces
- **Business Logic**: Shared calculations and validations

## 🛠️ Technology Stack & Rationale

### Backend Technologies

| Technology | Purpose | Why This Choice |
|------------|---------|-----------------|
| **Kotlin** | Programming Language | Type safety, coroutines, multiplatform support |
| **Ktor** | Web Framework | Lightweight, coroutine-based, Kotlin-native |
| **PostgreSQL** | Database | ACID compliance, JSON support, scalability |
| **Exposed** | ORM | Type-safe SQL, Kotlin DSL, coroutine support |
| **Flyway** | Database Migrations | Version control for database schema |
| **JWT** | Authentication | Stateless, scalable, industry standard |
| **BCrypt** | Password Hashing | Secure, adaptive, salt included |
| **Docker** | Containerization | Consistent deployment, scalability |

### Frontend Technologies

| Technology | Purpose | Why This Choice |
|------------|---------|-----------------|
| **Compose Multiplatform** | UI Framework | Modern declarative UI, cross-platform |
| **Material 3** | Design System | Modern, accessible, consistent UX |
| **SQLDelight** | Local Database | Type-safe SQL, multiplatform support |
| **Ktor Client** | HTTP Client | Consistent with server, coroutine support |
| **Kotlin Coroutines** | Async Programming | Structured concurrency, readable code |

### Development Tools

| Tool | Purpose | Why This Choice |
|------|---------|-----------------|
| **Gradle** | Build System | Kotlin DSL, dependency management, plugins |
| **Detekt** | Code Analysis | Code quality, consistent style |
| **Kotest** | Testing Framework | Kotlin-native, expressive assertions |
| **Testcontainers** | Integration Testing | Real database testing, isolation |

## 📊 Database Design

### Core Entities

#### Users Table
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pin VARCHAR(255) NOT NULL,  -- BCrypt hashed PIN
    role VARCHAR(20) CHECK (role IN ('admin', 'manager', 'staff')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);
```

#### Products Table (Jewelry-Specific)
```sql
CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    image_url VARCHAR(255),
    type VARCHAR(20) CHECK (type IN ('ring', 'bracelet', 'necklace', 'earring', 'other')),
    karat INTEGER NOT NULL,
    weight_grams DECIMAL(10, 2) NOT NULL,
    design_fee DECIMAL(10, 2) NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL,
    quantity_in_stock INTEGER DEFAULT 0
);
```

#### Sales & Transactions
```sql
CREATE TABLE sales (
    id VARCHAR(36) PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    synced BOOLEAN DEFAULT TRUE
);

CREATE TABLE sale_items (
    id VARCHAR(36) PRIMARY KEY,
    sale_id VARCHAR(36) REFERENCES sales(id),
    product_id VARCHAR(36) REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    purchase_price DECIMAL(10, 2),  -- For profit calculation
    design_fee DECIMAL(10, 2),
    profit DECIMAL(10, 2)
);
```

#### Gold Intake & Vendor Management
```sql
CREATE TABLE vendors (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_info TEXT,
    total_liability_balance DECIMAL(12, 2) DEFAULT 0.00,
    total_paid DECIMAL(12, 2) DEFAULT 0.00,
    total_intake_value DECIMAL(12, 2) DEFAULT 0.00
);

CREATE TABLE gold_intakes (
    id VARCHAR(36) PRIMARY KEY,
    vendor_id VARCHAR(36) REFERENCES vendors(id),
    party_type VARCHAR(20) CHECK (party_type IN ('seller', 'customer')),
    party_name VARCHAR(255) NOT NULL,
    karat INTEGER NOT NULL,
    grams DECIMAL(10, 3) NOT NULL,
    design_fee_per_gram DECIMAL(10, 2) NOT NULL,
    metal_value_per_gram DECIMAL(10, 2) DEFAULT 0.00,
    total_design_fee_paid DECIMAL(12, 2) NOT NULL,
    total_metal_value_owed DECIMAL(12, 2) DEFAULT 0.00
);
```

### Migration Strategy

The database uses Flyway migrations for version control:

1. **V1**: Initial schema (basic product/sales system)
2. **V2**: Jewelry workflow (specialized product fields)
3. **V3**: PIN-based authentication with roles
4. **V4**: Gold intake and vendor management

This evolutionary approach allows the system to grow while maintaining data integrity.

## 🚀 Setup & Development Guide

### Prerequisites

Before starting, ensure you have:

- **Java 21** (Eclipse Temurin recommended)
- **PostgreSQL 14+** (for server development)
- **Docker & Docker Compose** (for containerized deployment)
- **Git** (for version control)
- **IDE**: IntelliJ IDEA (recommended) or any Kotlin-capable IDE

### Development Environment Setup

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd almasa-suite
```

#### 2. Database Setup

**Option A: Local PostgreSQL**
```bash
# Install PostgreSQL (macOS with Homebrew)
brew install postgresql
brew services start postgresql

# Create database
createdb almasa

# Create user (optional)
psql -c "CREATE USER almasa WITH PASSWORD 'almasa';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE almasa TO almasa;"
```

**Option B: Docker PostgreSQL**
```bash
# Create docker-compose.yml for development
cat > docker-compose.dev.yml << EOF
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: almasa
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
EOF

# Start database
docker-compose -f docker-compose.dev.yml up -d
```

#### 3. Server Configuration

Create `server/src/main/resources/application-dev.conf`:
```hocon
ktor {
    development = true
}

db {
    url = "jdbc:postgresql://localhost:5432/almasa"
    user = "postgres"
    password = "postgres"
}

jwt {
    secret = "dev-jwt-secret-change-in-production"
}
```

#### 4. Build and Run

**Build the entire project:**
```bash
./gradlew build
```

**Run the server:**
```bash
./gradlew :server:run
```

**Run the desktop POS:**
```bash
./gradlew :pos-desktop:run
```

**Create desktop distributions:**
```bash
./gradlew :pos-desktop:createDistributable
# Find outputs in pos-desktop/build/compose/binaries/main/
```

### Default Login Credentials

The system comes with default users:
- **Admin**: PIN `0000`
- **Manager**: PIN `1111`
- **Staff**: PIN `2222`

⚠️ **Change these in production!**

## 🐳 Docker Deployment

### Development with Docker Compose

```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: almasa
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data

  almasa-server:
    build: .
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/almasa
      DB_USER: postgres
      DB_PASSWORD: postgres
      JWT_SECRET: your-production-jwt-secret
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - minio

volumes:
  postgres_data:
  minio_data:
```

### Building and Running

```bash
# Build the server JAR
./gradlew :server:buildFatJar

# Build Docker image
docker build -t almasa-suite:latest .

# Run with Docker Compose
docker-compose up -d
```

## 🏭 Production Deployment

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_URL` | PostgreSQL connection string | `jdbc:postgresql://localhost:5432/almasa` | Yes |
| `DB_USER` | Database username | `postgres` | Yes |
| `DB_PASSWORD` | Database password | `postgres` | Yes |
| `JWT_SECRET` | JWT signing secret | `dev-secret` | Yes |
| `STRIPE_API_KEY` | Stripe API key for payments | - | No |
| `MINIO_ENDPOINT` | MinIO endpoint for file storage | `http://minio:9000` | No |
| `MINIO_ACCESS_KEY` | MinIO access key | `minioadmin` | No |
| `MINIO_SECRET_KEY` | MinIO secret key | `minioadmin` | No |

### Security Considerations

1. **Change Default PINs**: Update all default user PINs
2. **Strong JWT Secret**: Use a cryptographically secure random string
3. **Database Security**: Use strong passwords, enable SSL
4. **Network Security**: Use HTTPS, restrict database access
5. **Regular Updates**: Keep dependencies updated
6. **Backup Strategy**: Implement regular database backups

### Monitoring & Logging

The application includes:
- **Health Check Endpoint**: `GET /health`
- **Structured Logging**: JSON logs with correlation IDs
- **Metrics**: Ready for Prometheus integration
- **Error Tracking**: Comprehensive exception handling

### Backup Strategy

```bash
# Database backup
pg_dump -h localhost -U postgres almasa > backup_$(date +%Y%m%d_%H%M%S).sql

# Automated backup script
#!/bin/bash
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h $DB_HOST -U $DB_USER $DB_NAME | gzip > $BACKUP_DIR/almasa_$DATE.sql.gz

# Keep only last 30 days
find $BACKUP_DIR -name "almasa_*.sql.gz" -mtime +30 -delete
```

## 📚 Developer Learning Guide

### Understanding the Architecture

This project demonstrates several important architectural patterns:

#### 1. **Clean Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  (UI Components, ViewModels, Controllers)                   │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                        │
│  (Use Cases, Services, Business Logic)                      │
├─────────────────────────────────────────────────────────────┤
│                    Domain Layer                             │
│  (Entities, Value Objects, Domain Services)                 │
├─────────────────────────────────────────────────────────────┤
│                    Infrastructure Layer                     │
│  (Database, External APIs, File System)                     │
└─────────────────────────────────────────────────────────────┘
```

#### 2. **Repository Pattern**
```kotlin
// Abstract repository in shared module
interface ProductRepository {
    suspend fun getAllProducts(): List<JewelryProduct>
    suspend fun getProductById(id: String): JewelryProduct?
    suspend fun createProduct(product: JewelryProductRequest): JewelryProduct
    suspend fun updateProduct(id: String, product: JewelryProductRequest): JewelryProduct
    suspend fun deleteProduct(id: String): Boolean
}

// Server implementation
class DatabaseProductRepository : ProductRepository {
    override suspend fun getAllProducts(): List<JewelryProduct> {
        return transaction {
            ProductTable.selectAll().map { it.toJewelryProduct() }
        }
    }
}

// Client implementation
class LocalProductRepository : ProductRepository {
    override suspend fun getAllProducts(): List<JewelryProduct> {
        return database.productQueries.selectAll().executeAsList()
            .map { it.toJewelryProduct() }
    }
}
```

#### 3. **MVVM Pattern (Client)**
```kotlin
class JewelryPosViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<JewelryProduct>>(emptyList())
    val products: StateFlow<List<JewelryProduct>> = _products.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _products.value = productRepository.getAllProducts()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### Creating a Similar Project

#### Step 1: Project Structure Setup

```bash
# Create project structure
mkdir jewelry-pos
cd jewelry-pos

# Initialize Gradle project
gradle init --type kotlin-application --dsl kotlin

# Create modules
mkdir -p shared/src/commonMain/kotlin
mkdir -p server/src/main/kotlin
mkdir -p pos-desktop/src/commonMain/kotlin
```

#### Step 2: Configure Gradle

**Root `build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.composeMultiplatform) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
```

**`settings.gradle.kts`:**
```kotlin
rootProject.name = "jewelry-pos"

include(":shared")
include(":server") 
include(":pos-desktop")
```

#### Step 3: Shared Module Setup

**`shared/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.yourcompany.jewelry.db")
        }
    }
}
```

#### Step 4: Server Module Setup

**`server/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("com.yourcompany.jewelry.ApplicationKt")
}

dependencies {
    implementation(projects.shared)
    
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    
    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgres.jdbc)
    implementation(libs.flyway.core)
}
```

#### Step 5: Desktop Module Setup

**`pos-desktop/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.shared)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.yourcompany.jewelry.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "JewelryPOS"
            packageVersion = "1.0.0"
        }
    }
}
```

### Best Practices Demonstrated

#### 1. **Error Handling**
```kotlin
// Comprehensive error handling in server
install(StatusPages) {
    exception<SerializationException> { call, cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Invalid request format")
        )
    }
    
    exception<SQLException> { call, cause ->
        call.respond(
            HttpStatusCode.InternalServerError,
            mapOf("error" to "Database error")
        )
    }
}
```

#### 2. **Type-Safe Configuration**
```kotlin
data class AppConfig(
    val database: DatabaseConfig,
    val jwt: JwtConfig,
    val server: ServerConfig
)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val driver: String
)
```

#### 3. **Coroutine Usage**
```kotlin
class SalesService {
    suspend fun processSale(sale: Sale): Result<Sale> = withContext(Dispatchers.IO) {
        try {
            transaction {
                // Database operations
                val savedSale = SaleTable.insert {
                    it[id] = sale.id
                    it[total] = sale.total
                    it[date] = sale.date
                }
                // Update inventory
                updateInventory(sale.items)
                Result.success(savedSale.toSale())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 4. **Offline-First Design**
```kotlin
class ProductSyncService {
    suspend fun syncProducts() {
        try {
            val remoteProducts = apiClient.getProducts()
            localDatabase.replaceProducts(remoteProducts)
            markAsSynced()
        } catch (e: NetworkException) {
            // Continue with local data
            logger.warn("Sync failed, using local data", e)
        }
    }
}
```

### Common Pitfalls & Solutions

#### 1. **Database Connection Management**
❌ **Wrong:**
```kotlin
fun getProducts(): List<Product> {
    val connection = DriverManager.getConnection(url)
    // Connection never closed!
    return connection.prepareStatement("SELECT * FROM products").executeQuery()
}
```

✅ **Correct:**
```kotlin
suspend fun getProducts(): List<Product> = transaction {
    ProductTable.selectAll().map { it.toProduct() }
}
```

#### 2. **State Management in Compose**
❌ **Wrong:**
```kotlin
@Composable
fun ProductList() {
    var products by remember { mutableStateOf(emptyList<Product>()) }
    
    // This will run on every recomposition!
    products = loadProducts()
}
```

✅ **Correct:**
```kotlin
@Composable
fun ProductList(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
}
```

#### 3. **Error Handling in Coroutines**
❌ **Wrong:**
```kotlin
viewModelScope.launch {
    val result = apiCall() // Can crash the app
    updateUI(result)
}
```

✅ **Correct:**
```kotlin
viewModelScope.launch {
    try {
        val result = apiCall()
        updateUI(result)
    } catch (e: Exception) {
        handleError(e)
    }
}
```

### Extension Points

The architecture supports easy extension:

#### 1. **Adding New Product Types**
```kotlin
// Add to JewelryType enum
enum class JewelryType {
    RING, BRACELET, NECKLACE, EARRING, WATCH, PENDANT, OTHER
}
```

```sql
-- Update database migration
ALTER TABLE products ADD CONSTRAINT check_type 
CHECK (type IN ('ring', 'bracelet', 'necklace', 'earring', 'watch', 'pendant', 'other'));
```

#### 2. **Adding New Payment Methods**
```kotlin
// Extend PaymentMethod enum
enum class PaymentMethod {
    CASH, CARD, BANK_TRANSFER, CRYPTO, INSTALLMENT
}

// Add payment processing logic
interface PaymentProcessor {
    suspend fun processPayment(amount: Double, method: PaymentMethod): PaymentResult
}
```

#### 3. **Adding Reporting Features**
```kotlin
// Create new report service
class ReportingService {
    suspend fun generateSalesReport(period: DateRange): SalesReport
    suspend fun generateInventoryReport(): InventoryReport
    suspend fun generateProfitReport(period: DateRange): ProfitReport
}
```

## 🧪 Testing Strategy

### Unit Testing
```kotlin
class ProductServiceTest {
    @Test
    fun `should calculate total price correctly`() {
        val product = JewelryProduct(
            id = "test",
            sku = "TEST001",
            type = JewelryType.RING,
            karat = 18,
            weightGrams = 5.0,
            designFee = 100.0,
            purchasePrice = 200.0,
            quantityInStock = 1
        )
        
        product.totalPrice shouldBe 300.0
    }
}
```

### Integration Testing
```kotlin
@Testcontainers
class ProductRepositoryTest {
    @Container
    val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")
    
    @Test
    fun `should save and retrieve product`() = runTest {
        val repository = DatabaseProductRepository(database)
        val product = createTestProduct()
        
        val saved = repository.createProduct(product)
        val retrieved = repository.getProductById(saved.id)
        
        retrieved shouldNotBe null
        retrieved?.sku shouldBe product.sku
    }
}
```

### UI Testing
```kotlin
@Test
fun `should display products in list`() = runComposeUiTest {
    val viewModel = mockk<ProductViewModel>()
    every { viewModel.products } returns flowOf(testProducts)
    
    setContent {
        ProductListScreen(viewModel)
    }
    
    onNodeWithText("Test Product").assertIsDisplayed()
}
```

## 📈 Performance Considerations

### Database Optimization
- **Indexes**: All foreign keys and frequently queried columns
- **Connection Pooling**: HikariCP for connection management
- **Query Optimization**: Use Exposed DSL for type-safe queries
- **Pagination**: Implement for large datasets

### Client Performance
- **Lazy Loading**: Load data as needed
- **Caching**: Cache frequently accessed data
- **Background Sync**: Sync data in background threads
- **Memory Management**: Proper lifecycle management

### Network Optimization
- **Compression**: Enable GZIP compression
- **Caching Headers**: Implement proper HTTP caching
- **Batch Operations**: Batch multiple operations
- **Connection Reuse**: HTTP/2 and connection pooling

## 🚀 Continuous Integration & Deployment (CI/CD)

### What is CI/CD?

**Continuous Integration (CI)** and **Continuous Deployment (CD)** are software development practices that help teams deliver code changes more frequently and reliably.

#### Why CI/CD Matters

For beginners, think of CI/CD as an automated quality control system:

1. **Continuous Integration**: Every time you push code, the system automatically:
   - Builds your project
   - Runs all tests
   - Checks code quality
   - Reports any issues immediately

2. **Continuous Deployment**: If all checks pass, the system can automatically:
   - Package your application
   - Deploy it to servers
   - Make it available to users

#### Benefits for Development Teams

- **Early Bug Detection**: Issues are caught immediately, not weeks later
- **Consistent Quality**: Every code change goes through the same quality checks
- **Faster Releases**: Automated processes eliminate manual deployment steps
- **Team Confidence**: Developers can make changes knowing the safety net will catch problems
- **Documentation**: The CI process serves as living documentation of how to build/deploy

### Our GitHub Actions Workflow Explained

Our project uses **GitHub Actions** for CI/CD. Let's break down every step:

#### Workflow File Location
```
.github/workflows/ci.yml
```

This file tells GitHub what to do when code changes occur.

#### Workflow Triggers
```yaml
on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master ]
```

**What this means**: The workflow runs when:
- Someone pushes code to `main`, `master`, or `develop` branches
- Someone creates a pull request targeting `main` or `master`

#### Job 1: Build and Test

This job runs on every code change and performs quality checks:

```yaml
build-and-test:
  runs-on: ubuntu-latest
```

**Explanation**: Creates a fresh Ubuntu Linux virtual machine for testing.

##### Step-by-Step Breakdown

**Step 1: Checkout Code**
```yaml
- name: Checkout code
  uses: actions/checkout@v4
```
- **Purpose**: Downloads your code to the virtual machine
- **Why v4**: Latest stable version with security updates and performance improvements
- **Beginner Tip**: This is like doing `git clone` on the CI server

**Step 2: Set up Java**
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
```
- **Purpose**: Installs Java 21 (required for Kotlin compilation)
- **Why Temurin**: Eclipse Temurin is a reliable, free OpenJDK distribution
- **Beginner Tip**: Like installing Java on your computer, but automated

**Step 3: Setup Gradle**
```yaml
- name: Setup Gradle
  uses: gradle/gradle-build-action@v3
  with:
    gradle-version: 8.9
```
- **Purpose**: Sets up Gradle build tool with caching and optimization
- **Why v3**: Latest version with improved caching and build insights
- **Benefits**: Faster builds through intelligent caching

**Step 4: Cache Dependencies**
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
```
- **Purpose**: Saves downloaded dependencies between runs
- **Why Important**: Dramatically speeds up builds (from minutes to seconds)
- **How it Works**: Creates a unique key based on your build files; if unchanged, reuses cached dependencies

**Step 5: Code Quality Check**
```yaml
- name: Run Detekt
  run: ./gradlew detekt
```
- **Purpose**: Analyzes code for potential issues, style violations, and complexity
- **What Detekt Checks**:
  - Code complexity (functions that are too complex)
  - Potential bugs (null pointer risks, etc.)
  - Style consistency (naming conventions, formatting)
  - Performance issues (inefficient code patterns)
- **Beginner Tip**: Like having an experienced developer review your code automatically

**Step 6: Run Tests**
```yaml
- name: Run tests
  run: ./gradlew test
```
- **Purpose**: Executes all unit and integration tests
- **Why Critical**: Ensures new changes don't break existing functionality
- **What Happens**: Runs tests for all modules (server, shared, pos-desktop)

**Step 7: Build Project**
```yaml
- name: Build project
  run: ./gradlew build
```
- **Purpose**: Compiles all code and creates distributable artifacts
- **What's Created**: JAR files, documentation, and other build outputs
- **Verification**: Confirms the project can be successfully built from source

**Step 8: Generate Documentation**
```yaml
- name: Generate Dokka documentation
  run: ./gradlew dokkaHtmlMultiModule
```
- **Purpose**: Creates API documentation from code comments
- **Why Useful**: Keeps documentation in sync with code changes
- **Output**: HTML documentation that can be published

**Step 9: Upload Documentation**
```yaml
- name: Upload Dokka documentation
  uses: actions/upload-artifact@v4
  with:
    name: dokka-docs
    path: build/dokka
```
- **Purpose**: Saves generated documentation as a downloadable artifact
- **Access**: Available in GitHub Actions run summary for download

#### Job 2: Docker Build and Publish

This job only runs when code is pushed to main/master branches:

```yaml
build-and-publish-docker:
  needs: build-and-test
  runs-on: ubuntu-latest
  if: github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master')
```

**Key Points**:
- `needs: build-and-test`: Only runs if the first job succeeds
- `if` condition: Only runs for pushes to main/master (not pull requests)

##### Docker Steps Explained

**Build Server JAR**
```yaml
- name: Build server JAR
  run: ./gradlew :server:shadowJar
```
- **Purpose**: Creates a "fat JAR" containing all dependencies
- **Why shadowJar**: Packages everything needed to run the server in one file

**Set up Docker Buildx**
```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
```
- **Purpose**: Enables advanced Docker features like multi-platform builds
- **Benefits**: Better caching, faster builds, cross-platform support

**Login to Container Registry**
```yaml
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```
- **Purpose**: Authenticates with GitHub's container registry
- **Security**: Uses GitHub's built-in token (no manual secrets needed)
- **Where Images Go**: `ghcr.io/your-username/your-repo`

**Build and Push Docker Image**
```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: ./server
    push: true
    tags: ghcr.io/${{ github.repository }}/almasa-server:sha-${{ github.sha }}
    cache-from: type=gha
    cache-to: type=gha,mode=max
```
- **Purpose**: Creates Docker image and uploads to registry
- **Tagging**: Uses git commit SHA for unique versioning
- **Caching**: Reuses layers between builds for speed

### Setting Up GitHub Actions from Scratch

#### For Complete Beginners

**Step 1: Understanding the Basics**

GitHub Actions is like having a robot that watches your code repository and automatically performs tasks when things change.

**Step 2: Create the Workflow Directory**
```bash
mkdir -p .github/workflows
```

**Step 3: Create Your First Workflow**
```yaml
# .github/workflows/simple-ci.yml
name: Simple CI

on:
  push:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run a simple command
        run: echo "Hello, World!"
```

**Step 4: Commit and Push**
```bash
git add .github/workflows/simple-ci.yml
git commit -m "Add simple CI workflow"
git push
```

**Step 5: Watch It Run**
- Go to your GitHub repository
- Click the "Actions" tab
- See your workflow running!

#### Advanced Setup for Kotlin Projects

**Step 1: Copy Our Workflow**
Use our `ci.yml` as a starting point for Kotlin/Gradle projects.

**Step 2: Customize for Your Project**
```yaml
# Change these to match your project
- name: Build project
  run: ./gradlew build  # Your build command

- name: Run tests
  run: ./gradlew test   # Your test command
```

**Step 3: Add Project-Specific Steps**
```yaml
# Example: Database migrations
- name: Run database migrations
  run: ./gradlew flywayMigrate

# Example: Security scanning
- name: Run security scan
  uses: securecodewarrior/github-action-add-sarif@v1
```

### Troubleshooting Common CI Issues

#### Build Failures

**Problem**: "Task 'build' not found"
```
Solution: Check your build.gradle.kts file exists and is valid
```

**Problem**: "Java version mismatch"
```yaml
# Fix: Ensure CI Java version matches your project
- name: Set up JDK 21  # Change to your version
  uses: actions/setup-java@v4
  with:
    java-version: '21'  # Must match your project
```

**Problem**: "Out of memory during build"
```yaml
# Fix: Add memory settings
- name: Build with more memory
  run: ./gradlew build -Xmx2g
  env:
    GRADLE_OPTS: "-Xmx2g -XX:MaxMetaspaceSize=512m"
```

#### Test Failures

**Problem**: "Tests pass locally but fail in CI"
```
Common causes:
1. Different timezone (use UTC in tests)
2. Different file system (case sensitivity)
3. Missing test dependencies
4. Race conditions in parallel tests
```

**Solution**: Run tests in Docker locally to match CI environment:
```bash
docker run --rm -v $(pwd):/app -w /app openjdk:21 ./gradlew test
```

#### Dependency Issues

**Problem**: "Could not resolve dependency"
```yaml
# Fix: Clear cache and retry
- name: Clear Gradle cache
  run: |
    rm -rf ~/.gradle/caches
    ./gradlew build --refresh-dependencies
```

**Problem**: "Network timeout downloading dependencies"
```yaml
# Fix: Add retry logic
- name: Build with retry
  uses: nick-invision/retry@v2
  with:
    timeout_minutes: 10
    max_attempts: 3
    command: ./gradlew build
```

#### Docker Issues

**Problem**: "Docker build fails"
```dockerfile
# Common fix: Multi-stage build for smaller images
FROM openjdk:21-jdk-slim as builder
COPY . .
RUN ./gradlew build

FROM openjdk:21-jre-slim
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Problem**: "Permission denied in Docker"
```yaml
# Fix: Set proper permissions
- name: Fix permissions
  run: chmod +x ./gradlew
```

### Best Practices for CI/CD

#### 1. Keep Builds Fast
```yaml
# Use parallel execution
- name: Run tests in parallel
  run: ./gradlew test --parallel

# Cache everything possible
- uses: actions/cache@v4
  with:
    path: ~/.gradle/caches
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
```

#### 2. Fail Fast
```yaml
# Run quick checks first
jobs:
  quick-checks:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Lint code
        run: ./gradlew detekt
      - name: Check formatting
        run: ./gradlew ktlintCheck

  full-build:
    needs: quick-checks  # Only run if quick checks pass
    runs-on: ubuntu-latest
    # ... full build steps
```

#### 3. Use Matrix Builds for Multiple Environments
```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest, macos-latest]
    java: [17, 21]
runs-on: ${{ matrix.os }}
steps:
  - name: Set up JDK ${{ matrix.java }}
    uses: actions/setup-java@v4
    with:
      java-version: ${{ matrix.java }}
```

#### 4. Secure Your Workflows

**Use Specific Action Versions:**
```yaml
# ✅ Good - use specific versions
- uses: actions/checkout@v4
- uses: actions/setup-java@v4

# ❌ Avoid - using latest/main is risky
# - uses: actions/checkout@main
```

**Limit Permissions:**
```yaml
# Add to your workflow file
permissions:
  contents: read
  packages: write
```

**Use Secrets for Sensitive Data:**
```yaml
# In your workflow steps
env:
  API_KEY: ${{ secrets.API_KEY }}
  DATABASE_PASSWORD: ${{ secrets.DB_PASSWORD }}
```

#### 5. Monitor and Alert
```yaml
# Add Slack notifications
- name: Notify on failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: failure
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### Security Considerations

#### Secrets Management
```bash
# Add secrets in GitHub repository settings
# Settings > Secrets and variables > Actions

# Use in workflow
env:
  DATABASE_PASSWORD: ${{ secrets.DB_PASSWORD }}
  JWT_SECRET: ${{ secrets.JWT_SECRET }}
```

#### Dependency Security
```yaml
# Add dependency scanning
- name: Run dependency check
  uses: dependency-check/Dependency-Check_Action@main
  with:
    project: 'almasa-suite'
    path: '.'
    format: 'ALL'
```

#### Container Security
```yaml
# Scan Docker images
- name: Scan Docker image
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: 'ghcr.io/${{ github.repository }}/almasa-server:latest'
    format: 'sarif'
    output: 'trivy-results.sarif'
```

### Advanced CI/CD Patterns

#### 1. Feature Branch Workflows
```yaml
# Different actions for different branches
on:
  push:
    branches: [main]  # Deploy to production
  pull_request:
    branches: [main]  # Run tests only

jobs:
  test:
    if: github.event_name == 'pull_request'
    # ... test steps

  deploy:
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    # ... deployment steps
```

#### 2. Environment-Specific Deployments
```yaml
jobs:
  deploy-staging:
    if: github.ref == 'refs/heads/develop'
    environment: staging
    # ... deploy to staging

  deploy-production:
    if: github.ref == 'refs/heads/main'
    environment: production
    # ... deploy to production
```

#### 3. Manual Approval Gates
```yaml
deploy-production:
  environment: 
    name: production
    url: https://almasa-suite.com
  # Requires manual approval in GitHub
```

### Monitoring Your CI/CD Pipeline

#### Key Metrics to Track
1. **Build Success Rate**: Percentage of successful builds
2. **Build Duration**: How long builds take
3. **Test Coverage**: Percentage of code covered by tests
4. **Deployment Frequency**: How often you deploy
5. **Mean Time to Recovery**: How quickly you fix issues

#### GitHub Actions Insights
- Go to repository > Insights > Actions
- View workflow run times, success rates, and trends
- Identify bottlenecks and optimization opportunities

### Learning Resources

#### Official Documentation
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Action](https://github.com/gradle/gradle-build-action)
- [Docker Build Push Action](https://github.com/docker/build-push-action)

#### Tutorials for Beginners
1. **GitHub Actions Basics**: Start with simple workflows
2. **Kotlin CI/CD**: Focus on Gradle and testing
3. **Docker Deployment**: Learn containerization
4. **Security Best Practices**: Secure your pipelines

#### Community Examples
- [Awesome Actions](https://github.com/sdras/awesome-actions)
- [GitHub Actions Examples](https://github.com/actions/example-workflows)

## 🔧 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check PostgreSQL status
brew services list | grep postgresql

# Test connection
psql -h localhost -U postgres -d almasa -c "SELECT 1;"

# Check logs
tail -f /usr/local/var/log/postgresql@14.log
```

#### Build Issues
```bash
# Clean build
./gradlew clean build

# Check Java version
java -version

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.5
```

#### Desktop App Issues
```bash
# Check compose version compatibility
./gradlew :pos-desktop:dependencies | grep compose

# Run with debug logging
./gradlew :pos-desktop:run --debug
```

#### CI/CD Issues
```bash
# Test workflow locally with act
brew install act
act -j build-and-test

# Debug workflow with tmate
- name: Debug with tmate
  uses: mxschmitt/action-tmate@v3
  if: failure()
```

## 🎯 Future Enhancements

### Planned Features
- **Mobile App**: iOS/Android companion app
- **Web Dashboard**: Browser-based admin interface
- **Advanced Analytics**: Machine learning insights
- **Multi-Store Support**: Chain store management
- **Integration APIs**: Third-party system integration

### Technical Improvements
- **Microservices**: Split into smaller services
- **Event Sourcing**: Implement event-driven architecture
- **GraphQL**: Add GraphQL API alongside REST
- **Real-time Updates**: WebSocket support
- **Advanced Security**: OAuth2, rate limiting

## 📝 Contributing

### Development Workflow
1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Make changes and add tests
4. Run quality checks: `./gradlew check`
5. Commit changes: `git commit -m 'Add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Create Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Write tests for new features
- Run Detekt before committing

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **JetBrains** for Kotlin and Compose Multiplatform
- **Ktor Team** for the excellent web framework
- **PostgreSQL Community** for the robust database
- **Open Source Community** for the amazing libraries

---

**Built with ❤️ using Kotlin Multiplatform**

For questions, issues, or contributions, please visit our [GitHub repository](https://github.com/your-org/almasa-suite).