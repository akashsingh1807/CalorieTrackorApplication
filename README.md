# CalorieTrackor Application

## Complete Architecture & Project Structure

### 1. High-Level Design (HLD)
The CalorieTrackerApplication is a full-stack web application designed to help users track their daily nutritional intake, water consumption, and body weight. 
The system architecture follows a classic Client-Server model.
- **Frontend**: A mobile application built with Kotlin Multiplatform (KMP) and Compose Multiplatform. It natively targets Android (and iOS) utilizing an MVVM architecture, providing an intuitive dashboard, food logging capabilities, and AI-powered nutritional insights.
- **Backend**: A robust RESTful API built with Java and Spring Boot. It handles business logic, user authentication, and data persistence.
- **Database**: PostgreSQL (or similar relational database) for storing user profiles, daily logs, and food metadata.
- **AI Integration**: Integrates with Google's Gemini AI to parse natural language food descriptions and calculate precise macronutrients and calories.

#### Data Flow
1. **User Action**: The user interacts with the UI (e.g., enters "2 boiled eggs and toast" in the AI Insights page).
2. **Frontend Request**: The KMP app (Android/iOS) makes an HTTP POST request to the Spring Boot backend (`/api/ai/text`) via its API client.
3. **Backend Processing**:
   - The Spring Security layer authenticates the JWT token.
   - The `AIController` routes the request to the `GeminiFoodInfoService`.
   - The service constructs a detailed prompt with strict schema instructions and calls the Gemini API via a `RestTemplate`.
4. **AI Inference**: Gemini processes the natural language, estimates macros, and returns a strictly formatted JSON response including a step-by-step reasoning block.
5. **Data Persistence**: The backend caches/saves the AI response in the database (`FoodInfoRepository`) to avoid redundant API calls for future exact queries.
6. **Frontend Update**: The backend returns the JSON to the frontend, which dynamically renders the estimated calories, protein, carbs, and fats inline in a clear UI format natively using Jetpack/Compose Multiplatform.

---

### 2. Low-Level Design (LLD)

#### Mobile Frontend (Kotlin Multiplatform)
- **Architecture**: MVVM (Model-View-ViewModel) pattern separating UI logic from business logic.
- **UI Framework**: Compose Multiplatform for shared UI across Android and iOS.
- **Local Data Storage**: Room Database used in `androidMain` with DAOs (`WaterDao`, `MealDao`, `WeightDao`) for robust offline caching.
- **Networking**: Configured API Client (`CalorieApiClient.kt`) to interface securely with the Spring Boot backend.
#### Authentication & Security
- **JWT (JSON Web Tokens)**: Used for stateless, scalable authentication.
- **Spring Security**: Configured with `SecurityConfig`, `JwtAuthenticationFilter`, and `AuthEntryPointJwt` to intercept and secure all private API endpoints.

#### Controllers & Routing
- `AuthController`: Handles registration, login, and JWT generation.
- `MealController`: Handles CRUD operations for daily meal logs.
- `AIController`: Exposes endpoints for AI text analysis.
- `WaterController`, `WeightController`, `AnalyticsController`: Manage corresponding user tracking features.

#### Services & Business Logic
- `GeminiFoodInfoService`: Handles the core logic of communicating with the Gemini API, structuring the system instruction to force JSON output with specific macros, estimating standard serving sizes, and validating the response.
- `NutritionService`: Aggregates user daily macros, calculates remaining goals, and drives the dashboard metrics.
- `AuthService`: Manages user registration, password hashing, and token issuance.

#### Data Models & Repositories (JPA)
- `User`: Stores user credentials and profile (age, weight, goal).
- `Meal`, `FoodItem`: Represents logged meals and individual food entries.
- `FoodInfo`: Caches AI responses locally.
- `WeightLog`, `WaterLog`: Time-series data for tracking metrics over time.

---

### 3. Project Structure

#### Backend (Spring Boot / Java)
```text
backend/src/main/java/com/calorie/tracker/
├── config/              # Application, Database, and Security configurations
├── controller/          # REST API endpoints (AuthController, AIController, MealController, etc.)
├── dto/                 # Data Transfer Objects for API requests/responses (GeminiFoodResponseDto, etc.)
├── migration/           # Database migration scripts
├── model/               # JPA Entities (User, Meal, FoodInfo, WeightLog, etc.)
├── repository/          # Spring Data JPA interfaces (UserRepository, MealRepository, etc.)
├── security/            # JWT filters, custom UserDetailsService, and security entry points
└── service/             # Business logic (GeminiFoodInfoService, AuthService, MealService, etc.)
```

#### Frontend (Kotlin Multiplatform / Android)
```text
android/composeApp/src/
├── commonMain/kotlin/com/calorie/tracker/
│   ├── core/            # Core networking (CalorieApiClient) and UI elements (SpeechRecognizer, ImagePicker)
│   ├── feature_auth/    # Authentication ViewModels, Repositories, and Screens (AuthScreen, OnboardingScreen)
│   ├── feature_journal/ # Core features (Dashboard, WaterTracker, WeightTracker) and their ViewModels/Repositories
│   ├── model/           # Shared Kotlin data models mapping to backend DTOs
│   └── ui/              # Global Compose theme (Color, Shape, Type, Components)
├── androidMain/kotlin/com/calorie/tracker/
│   ├── core/database/   # Local Room Database configuration (AppDatabase)
│   ├── feature_*/data/  # Android-specific local data sources and DAOs (MealDao, WaterDao, WeightDao)
│   └── MainActivity.kt  # Android application entry point
└── iosMain/kotlin/com/calorie/tracker/
    └── feature_*/data/  # iOS-specific implementations and bindings
```

---

### 4. AI Feature Integration (Gemini)
The application features a clinical nutritional analysis engine powered by Gemini.
- **Prompt Strategy**: The system forces a strict JSON output format ensuring properties like `reasoning`, `isFood`, `foodName`, `calories`, `protein`, `carbohydrates`, and `fat` are always predictably structured.
- **Inline UI**: Results are presented inline directly within the AI Insights page. The visual camera/image upload functionality has been removed in favor of a clean, text-based workflow that shows AI reasoning in a raw text box seamlessly on the same page.
