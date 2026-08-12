# StudyHubApp

Aplicación multiplataforma de estudio para estudiantes. Construida con **Kotlin Multiplatform + Compose Multiplatform**, comparte la interfaz y la lógica entre **Android**, **iOS** y **Web**.

## Funcionalidades

- **Autenticación**: login con email/contraseña y **OAuth con Google**, gestión de tokens, almacenamiento seguro.
- **Inicio (Home)**: resumen de la vida académica del estudiante.
- **Asignaturas**: listado y detalle de materias y tareas.
- **Tutor**: asistente de estudio conversacional.
- **Quizzes**: modos de práctica y repaso.
- **Roadmaps**: rutas de aprendizaje por objetivos.
- **Grupos y notificaciones**: estudio en grupo y centro de notificaciones.
- **Perfil**: gestión de la cuenta del usuario.
- **Soporte offline**: almacenamiento local y sincronización.
- **Tema y estilos**: UI en Compose Multiplatform con iconografía propia y renderizado de contenido enriquecido.

## Arquitectura

| Módulo          | Descripción                                                       |
|-----------------|-------------------------------------------------------------------|
| `androidApp`    | Aplicación Android (entry point).                                 |
| `iosApp`        | Aplicación iOS (Xcode + SwiftUI/Compose).                         |
| `shared`        | Código común: UI (Compose), redes HTTP (Ktor), modelos, OAuth.    |
| `web`           | Target web (Compose para la web).                                 |
| `mockups`       | Maquetas HTML de la interfaz.                                     |

## Requisitos

- Android Studio / IntelliJ con plugin de Kotlin Multiplatform.
- JDK 11+.
- Xcode (solo para el target iOS).

## Ejecutar

- **Android**: `./gradlew :androidApp:assembleDebug` o la configuración de run del IDE.
- **iOS**: abre `iosApp/` en Xcode y ejecuta desde ahí.
- **Web**: configuración de run del IDE para el target `web`.

## Tests

```bash
./gradlew :shared:testAndroidHostTest   # Tests Android
./gradlew :shared:iosSimulatorArm64Test # Tests iOS
```

## Stack

- Kotlin Multiplatform + Compose Multiplatform
- Ktor Client (HTTP, Content Negotiation, JSON)
- kotlinx.serialization y kotlinx.coroutines
- Google OAuth
- Gradle (Kotlin DSL) + version catalog

## Estructura

```
.
├── androidApp/              # Aplicación Android
├── iosApp/                  # Aplicación iOS (Xcode)
├── shared/
│   └── src/commonMain/      # Código compartido (UI + red + modelos)
│       ├── kotlin/org/studyhub/project/
│       │   ├── App.kt       # Raíz de la app Compose
│       │   ├── ui/          # Pantallas Compose
│       │   └── net/         # ApiClient, OAuth, modelos, token y offline
├── web/                     # Target web
├── mockups/                 # Maquetas HTML
└── settings.gradle.kts
```

## Licencia

Privado — uso personal/proyecto académico.