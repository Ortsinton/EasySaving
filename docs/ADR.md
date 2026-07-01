# EasySaving — Architecture Decision Records

Este documento recoge las decisiones de arquitectura tomadas en la fase de diseño
del proyecto, con su contexto y los trade-offs considerados. Se actualizará según
evolucione el proyecto.

---

## ADR-001: Kotlin Multiplatform como estrategia de compartición de código

**Contexto:** El objetivo del proyecto es doble: servir de pieza de portfolio y
como vehículo de aprendizaje de KMP, Jetpack Compose moderno y SwiftUI moderno,
manteniendo dominio nativo real de ambas plataformas.

**Decisión:** Compartir domain, data y presentation (ViewModels) vía KMP. Mantener
la capa de UI 100% nativa (Jetpack Compose en Android, SwiftUI en iOS).

**Alternativas consideradas:**
- Compose Multiplatform (compartir también UI): descartado porque diluiría la
  demostración de dominio nativo de SwiftUI, que es uno de los objetivos
  explícitos del proyecto.
- Apps 100% nativas independientes (sin KMP): descartado porque no cumpliría el
  objetivo de practicar compartición de lógica de negocio.

**Consecuencias:** Mayor complejidad de build (dos toolchains, targets iOS/Android
en Gradle) a cambio de una demostración más completa de criterio arquitectónico:
qué se comparte y qué no, y por qué.

---

## ADR-002: SQLDelight como capa de persistencia local

**Contexto:** Se necesita una base de datos local, offline-first, accesible desde
Kotlin compartido y disponible en ambas plataformas.

**Decisión:** SQLDelight.

**Alternativas consideradas:**
- **Room (KMP):** ya soporta multiplatform, pero con menor recorrido en
  producción multiplataforma que SQLDelight a día de hoy. Además, SQLDelight
  genera APIs type-safe a partir de SQL explícito, lo cual demuestra manejo de
  SQL real (útil de cara a entrevista) en vez de abstraerlo completamente.

**Consecuencias:** Se gestionan migraciones de esquema versionadas manualmente
mediante los ficheros `.sqm` de SQLDelight — se documentará como punto fuerte de
portfolio (gestión de migraciones real, no solo un "modelo v1 hardcodeado").

---

## ADR-003: ViewModels compartidos en KMP (no solo domain/data)

**Contexto:** El ViewModel es, por definición, agnóstico de cómo se renderiza la
UI: decide qué se debe presentar, no cómo. Esto lo hace, en principio, apto para
compartición multiplataforma, más allá de domain y data.

**Decisión:** Compartir los ViewModels usando `androidx.lifecycle.ViewModel`
(multiplatform), exponiendo estado observable (`StateFlow`) y funciones de acción
(intents). La navegación queda excluida explícitamente de esta capa (ver ADR-004).

**Problemas identificados y solución adoptada:**

1. **Interoperabilidad `StateFlow` → Swift.** `Flow` no es idiomático en Swift.
   Se usa el plugin **SKIE** (Touchlab) para generar automáticamente
   equivalentes `async/await` y tipos Swift-friendly desde las coroutines/Flow
   expuestas por el ViewModel compartido.

2. **Ciclo de vida en iOS.** `ViewModelStoreOwner` no existe de forma nativa en
   iOS. El ciclo de vida de cada ViewModel compartido se gestiona explícitamente
   ligado al ciclo de vida de la `View` de SwiftUI (creación/liberación en
   `onAppear`/`onDisappear` o contenedor equivalente), evitando fugas de memoria
   por ViewModels no liberados.

3. **Puente hacia `@Observable` (SwiftUI moderno, iOS 17+).** Se implementa una
   clase Swift `@Observable` que se suscribe al `StateFlow` compartido (vía SKIE)
   y republica los cambios como propiedades observables nativas, permitiendo que
   las Views de SwiftUI usen el patrón de reactividad idiomático de Swift en
   lugar de observar un `Flow` directamente.

**Alternativas consideradas:**
- ViewModel nativo por plataforma, consumiendo únicamente los casos de uso
  compartidos: más simple y "seguro", pero duplica lógica de presentación
  (mapeo de estado, gestión de loading/error) en ambas plataformas.

**Consecuencias:** Mayor inversión inicial en el puente de interoperabilidad
(Sprint 1), a cambio de eliminar duplicación de lógica de presentación entre
plataformas y demostrar manejo de los problemas reales de compartir estado
reactivo entre paradigmas de UI distintos.

---

## ADR-004: Navegación 100% nativa, fuera del alcance de KMP

**Contexto:** La navegación es fuertemente dependiente del framework de UI
(`NavHost`/`NavController` en Compose vs `NavigationStack`/`NavigationPath` en
SwiftUI), y no existe una abstracción común madura que no termine forzando el
paradigma de una plataforma sobre la otra.

**Decisión:** El ViewModel compartido nunca navega ni conoce el grafo de
navegación. Expone únicamente funciones de acción (p. ej. `onTransactionSelected`)
que notifican una intención; cada plataforma decide, de forma nativa, si eso se
traduce en una navegación y cómo.

**Consecuencias:** Se mantiene la superficie de UI (incluida la navegación) como
demostración de dominio nativo idiomático en cada plataforma. Si en el futuro se
necesita evitar renavegaciones duplicadas en recomposición, se evaluará añadir un
canal de eventos de un solo uso (`SharedFlow`) — pendiente, no implementado en el
MVP.

---

## ADR-005: Koin como framework de inyección de dependencias

**Contexto:** Se necesita DI funcional en Kotlin compartido y consumible desde
Android e iOS.

**Decisión:** Koin.

**Alternativas consideradas:**
- Hilt: sin soporte multiplatform (limitado a Android/JVM), descartado por
  incompatibilidad directa con el objetivo de compartir domain/data/presentation.

**Consecuencias:** Configuración de módulos Koin compartida, con puntos de
entrada específicos por plataforma para inicializar el grafo de dependencias
desde `Application` (Android) y desde el punto de arranque de la app (iOS).

---

## ADR-006: Alcance del MVP — exclusión de presupuestos y sincronización remota

**Contexto:** El proyecto se desarrolla en paralelo a una búsqueda activa de
empleo, con disponibilidad de ~20-25h semanales.

**Decisión:** El MVP se limita a CRUD de transacciones, categorías y analíticas
locales (offline-first). Presupuestos con alertas, multi-cuenta, multi-moneda y
sincronización remota quedan fuera del MVP, documentados como roadmap futuro.

**Consecuencias:** Permite alcanzar un estado "presentable" en un plazo acotado
(~6 semanas) sin comprometer la profundidad de la demostración arquitectónica
en el núcleo del proyecto (KMP, Clean Architecture, testing, CI/CD).

---

*Próximas decisiones pendientes de documentar (a añadir cuando se resuelvan):*
- *Estrategia de eventos de un solo uso para navegación (si se adopta).*
- *Estrategia de testing de UI en SwiftUI (XCTest vs Snapshot testing).*
