# EasySaving — Contexto de proyecto para Claude Code

Este fichero se lee automaticamente al inicio de cada sesion de Claude Code.
Contiene el contexto minimo necesario para trabajar en cualquier tarea del
proyecto sin tener que reexplicar la arquitectura en cada chat.

## Que es EasySaving

App de finanzas personales (budgeting) desarrollada como proyecto de
portfolio. Permite registrar gastos/ingresos y visualizar analiticas
(por categoria, por fecha, tendencias). Proyecto KMP con Android (Compose)
e iOS (SwiftUI) nativos, compartiendo domain, data y presentation.

Documentacion completa de decisiones y estructura:
- `docs/ADR.md` — decisiones de arquitectura con contexto y trade-offs
- `docs/PROJECT_STRUCTURE.md` — estructura de modulos y convenciones

## Stack tecnico

- Kotlin Multiplatform (targets: android, iosArm64, iosSimulatorArm64)
- Persistencia local: SQLDelight (offline-first, sin backend)
- DI: Koin
- Async: Coroutines + Flow
- Interoperabilidad Swift: SKIE
- UI: Jetpack Compose (Android) y SwiftUI moderno con @Observable (iOS 17+)
- Testing compartido: kotlin.test + Turbine
- CI: GitHub Actions

## Frontera de compartición (regla mas importante)

- **Compartido:** domain (modelos, casos de uso), data (repositorios,
  SQLDelight), presentation (ViewModels)
- **Nativo:** UI completa (Compose / SwiftUI), navegacion, graficos

Los ViewModels compartidos NUNCA navegan. Exponen:
- Un `StateFlow<UiState>` con el estado a renderizar
- Funciones de accion (ej. `onTransactionSelected(id)`) que solo notifican
  intencion; cada plataforma decide si eso implica navegar y como

Ver ADR-003 y ADR-004 para el detalle completo y el porque de esta decision.

## Convenciones de codigo

- Paquetes: `com.easysaving.domain`, `com.easysaving.data`,
  `com.easysaving.presentation`, `com.easysaving.di`
- `shared/domain` no puede importar nada de Android, iOS ni SQLDelight
- `shared/presentation` no puede importar tipos de SQLDelight directamente
  (siempre pasa por domain)
- Toda logica de negocio (calculos, validaciones, agregaciones) vive en
  domain, nunca en un ViewModel ni en un Composable/View

## Flujo de trabajo por tarea

1. Cada tarea viene de una tarjeta de Trello (tablero EasySaving, lista
   "To Do") con Objetivo + Criterios de aceptacion + Referencias a ADR.
2. Crea una rama nueva por tarea: `task-N-nombre-corto`
3. Al terminar, verifica que el proyecto compila para ambos targets y que
   los tests pasan
4. Commit descriptivo referenciando la tarea, ej:
   `feat: definir modelos de dominio de dominio (Transaction, Category, Money)`
5. El progreso entre sesiones se seduce del codigo y el historial de git,
   no de conversaciones anteriores — cada sesion de Claude Code empieza sin
   memoria de sesiones previas, por eso este fichero y el codigo son la
   unica fuente de verdad

## Que NO hacer

- No compartir UI entre plataformas (nada de Compose Multiplatform):
  el objetivo del proyecto es demostrar dominio nativo de ambos toolkits
- No meter navegacion dentro de un ViewModel compartido
- No anadir features fuera del MVP (presupuestos, multi-cuenta,
  multi-moneda, sync remoto) salvo que se pida explicitamente — estan
  documentadas como roadmap futuro en el ADR