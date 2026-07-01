# EasySaving — Estructura de Módulos (Esqueleto)

Estructura de carpetas y módulos Gradle propuesta para el proyecto. Este
documento define **qué existe y su responsabilidad**, no su implementación.
Sirve como guía para el Sprint 0.

```
EasySaving/
│
├── shared/                              # Todo el código Kotlin compartido (KMP)
│   ├── build.gradle.kts                 # Targets: android, iosArm64, iosSimulatorArm64
│   │
│   ├── domain/                          # Módulo :shared:domain
│   │   ├── model/                       # Transaction, Category, Money, etc.
│   │   ├── repository/                  # Interfaces (TransactionRepository, CategoryRepository)
│   │   └── usecase/                     # AddTransactionUseCase, GetMonthlyAnalyticsUseCase...
│   │       # Sin dependencias de Android/iOS ni de frameworks de infraestructura.
│   │       # 100% testeable con kotlin.test puro.
│   │
│   ├── data/                            # Módulo :shared:data
│   │   ├── local/
│   │   │   ├── sqldelight/              # Ficheros .sq (esquema + queries)
│   │   │   └── EasySavingDatabase.kt    # Wrapper de acceso a la DB generada
│   │   ├── mapper/                      # Entity (SQLDelight) <-> Domain model
│   │   └── repository/                  # Implementaciones de las interfaces de domain
│   │
│   ├── presentation/                    # Módulo :shared:presentation
│   │   ├── transactionlist/
│   │   │   ├── TransactionListViewModel.kt
│   │   │   └── TransactionListUiState.kt
│   │   ├── transactionform/
│   │   │   ├── TransactionFormViewModel.kt
│   │   │   └── TransactionFormUiState.kt
│   │   └── analytics/
│   │       ├── AnalyticsViewModel.kt
│   │       └── AnalyticsUiState.kt
│   │       # Cada ViewModel expone StateFlow<UiState> + funciones de acción.
│   │       # Cero referencias a navegación (ver ADR-004).
│   │
│   └── di/                              # Módulos Koin compartidos
│       └── SharedModule.kt
│
├── androidApp/                          # Módulo :androidApp
│   ├── build.gradle.kts
│   └── src/main/kotlin/.../
│       ├── EasySavingApplication.kt     # Punto de arranque de Koin en Android
│       ├── navigation/                  # NavHost + NavGraph (Compose Navigation)
│       └── ui/
│           ├── transactionlist/         # Composables que consumen TransactionListViewModel
│           ├── transactionform/
│           └── analytics/               # Composables + gráficos (Vico u otra lib)
│
├── iosApp/                              # Proyecto Xcode
│   ├── iosApp.xcodeproj
│   └── iosApp/
│       ├── EasySavingApp.swift          # Punto de arranque de Koin en iOS
│       ├── Navigation/                  # NavigationStack + NavigationPath
│       ├── Bridges/                     # Puentes @Observable <-> StateFlow (ver ADR-003)
│       │   ├── ObservableTransactionListViewModel.swift
│       │   ├── ObservableTransactionFormViewModel.swift
│       │   └── ObservableAnalyticsViewModel.swift
│       └── Views/
│           ├── TransactionList/
│           ├── TransactionForm/
│           └── Analytics/               # Vistas + Swift Charts
│
├── .github/
│   └── workflows/
│       └── ci.yml                       # Matriz: test :shared (JVM+iOS simulator),
│                                         # build androidApp, build iosApp
│
├── docs/
│   ├── ADR.md                           # Este documento de decisiones
│   └── screenshots/                     # Capturas/gifs para el README
│
├── settings.gradle.kts                  # include(":shared", ":shared:domain",
│                                         #         ":shared:data", ":shared:presentation",
│                                         #         ":androidApp")
└── README.md
```

## Notas sobre dependencias entre módulos

```
:shared:domain        <-- no depende de nada dentro del proyecto
:shared:data          <-- depende de :shared:domain
:shared:presentation  <-- depende de :shared:domain (usa los casos de uso)
                          NO depende de :shared:data directamente (pasa por domain)
:shared:di            <-- conecta data + domain + presentation (grafo Koin)
:androidApp           <-- depende de :shared:presentation, :shared:di
iosApp (vía framework)<-- consume el binario compilado de :shared (todas las capas)
```

Esta separación fuerza que `presentation` nunca conozca detalles de
persistencia (SQLDelight), y que `domain` permanezca puro — es la frontera que
se puede señalar directamente en una entrevista para hablar de Clean
Architecture con un ejemplo real, no solo teórico.

## Convenciones de nombres de paquete

```
com.ortsinton.easysaving.domain.model
com.ortsinton.easysaving.domain.usecase
com.ortsinton.easysaving.data.local
com.ortsinton.easysaving.data.repository
com.ortsinton.easysaving.presentation.transactionlist
com.ortsinton.easysaving.di
```

## Próximo paso técnico (Sprint 0)

1. Crear el proyecto KMP (vía plantilla oficial de KMP o `kmp-nativecoroutines`
   template) con los targets `android`, `iosArm64`, `iosSimulatorArm64`.
2. Configurar SQLDelight en `:shared:data` con un esquema mínimo (una tabla
   `Transaction`) y verificar que genera código para ambos targets.
3. Configurar Koin con un módulo vacío inyectable desde `androidApp` y desde
   `iosApp`.
4. Configurar GitHub Actions con un job que compile `:shared` para ambos
   targets y corra un test dummy en verde.
5. Confirmar que el pipeline SKIE está integrado y genera interoperabilidad
   Swift a partir de un `Flow` de prueba antes de escribir el primer
   ViewModel real (Sprint 1).
