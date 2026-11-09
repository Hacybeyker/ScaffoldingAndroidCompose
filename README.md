# 🚀 ScaffoldingAndroidCompose

[![CI](https://github.com/hacybeyker/ScaffoldingAndroidCompose/actions/workflows/ci.yml/badge.svg)](https://github.com/hacybeyker/ScaffoldingAndroidCompose/actions/workflows/ci.yml)

> **Plantilla (scaffolding) para crear proyectos Android nativos con Jetpack Compose e infraestructura de IA lista para usar.**

Clona, ejecuta un script, y en menos de un minuto tienes un proyecto Android con tu nombre, tu package y reglas de arquitectura listas para que cualquier agente de IA (Claude Code, GitHub Copilot, Cursor, Junie, Antigravity…) trabaje con calidad profesional desde el primer prompt.

---

## ✨ ¿Qué incluye?

| Componente | Descripción |
|------------|-------------|
| **Jetpack Compose + Material 3** | UI declarativa con design tokens (Color, Type, Shape, Spacing) |
| **Arquitectura** | Clean Architecture + MVVM con UDF, organizada por features (perfil [`hb-stack-mobile-kotlin`](https://github.com/Hacybeyker/agent-skills)), con una feature de ejemplo completa |
| **Hilt + Navigation 3** | DI por feature (`@Binds`) y navegación type-safe (`NavKey @Serializable`) |
| **`init-project.sh`** | Script que renombra proyecto, package y applicationId en un solo paso |
| **`AGENTS.md`** | Reglas del proyecto para agentes de IA (estándar [agents.md](https://agents.md/)); `CLAUDE.md` lo importa para Claude Code |
| **Skills globales** | Las skills [hb-*](https://github.com/Hacybeyker/agent-skills) (implementar, revisar, commits, releases) se instalan una vez por máquina, no por proyecto |
| **Catálogo de versiones** | `gradle/libs.versions.toml` centralizado (Kotlin 2.4, AGP 9, Compose BOM), mantenido al día por Dependabot |
| **Calidad de código** | **ktlint + detekt + Android Lint** preconfigurados con tareas agregadas (`./gradlew formatAndAnalyze`) — reglas en `.editorconfig`, `config/detekt/detekt.yml` y `lint.xml`. Se configuran una vez en el `build.gradle.kts` raíz y se aplican a cada módulo Android, así que la plantilla está lista para sumar módulos (receta en `AGENTS.md`) |
| **Cobertura con gate** | **Kover** al 90% sobre `domain`/`data`/`ViewModels` (codegen y frontera de plataforma excluidos) |
| **Reportes HTML** | `./gradlew qualityReports` genera los cuatro (Lint, detekt, ktlint y Kover) e imprime sus rutas `file://` |
| **Análisis estático en la nube** | **SonarQube/SonarCloud opcional**, alimentado por los reportes de Lint/detekt/ktlint/Kover. Es opt-in: sin el secret `SONAR_TOKEN` el paso se salta y el CI queda verde |
| **Screenshot testing** | **Roborazzi + Robolectric**: goldens versionados en `app/src/test/screenshots/`, con gate `verifyRoborazziDebug` |
| **CI/CD (GitHub Actions)** | `ci.yml` (calidad → build/tests/gates/Sonar en cada push/PR) y `release.yml` (APK/AAB + GitHub Release al pushear un tag `v*`) + Dependabot. Actions **fijadas por SHA** y **Build Scan** por job en repos públicos |
| **Release endurecido** | **R8** activo (`optimization`), keep rules en `src/main/keepRules/`, `lint.abortOnError = true` |
| **Testing** | JUnit + Turbine + Fakes escritos a mano, con `MainDispatcherRule` y tests de ejemplo (UseCase + Repository + ViewModel + golden). Todo en JVM: **sin `androidTest`** |

## 🚫 Qué no incluye

La plantilla es la base de la app, no una app completa. Todo lo siguiente se agrega en
`gradle/libs.versions.toml` y, dentro de cada feature, en `data/` (el contrato de `domain/repository` no cambia):

| Área | Qué falta | Hoy |
|------|-----------|-----|
| **Red** | Retrofit, OkHttp, Ktor y `kotlinx-serialization-json` | `kotlinx-serialization-core` solo serializa las `NavKey`; no hay cliente HTTP ni parseo de JSON |
| **Persistencia** | Room y DataStore | La feature de ejemplo guarda el estado en memoria (`InMemoryGreetingRepository`) |
| **Imágenes** | Coil, Glide o similares | `material-icons-core` trae solo los íconos básicos |
| **Navigation Compose** | `NavHost` y `NavController` | La navegación es **Navigation 3** (`androidx.navigation3`: `NavDisplay`, `rememberNavBackStack` y `entryProvider`). Es otra librería, con otras APIs: los ejemplos de Navigation Compose no se copian tal cual |

Tampoco trae tests instrumentados (todo corre en la JVM) ni la firma de release (ver `release.yml`).

## 📋 Requisitos

- **JDK 21** (Gradle lo descarga automáticamente vía toolchain si no lo tienes)
- **Android Studio** (versión reciente)

## ⚡ Quick Start

```bash
# 1. Clona la plantilla con el nombre de tu nuevo proyecto
git clone https://github.com/hacybeyker/ScaffoldingAndroidCompose.git MiAppGenial
cd MiAppGenial

# 2. Ejecuta el inicializador (modo interactivo)
./init-project.sh
```

El script te preguntará el **nombre del proyecto**, el **package base**, el **nombre visible de la app** y la **licencia** (MIT por defecto), y hará todo el resto: renombrar archivos, mover paquetes, limpiar la feature de ejemplo, completar `AGENTS.md`, renombrar la carpeta raíz y dejar el historial de git limpio (squash automático si vienes del scaffolding, o commit encima si vienes de GitHub Template).

¿Prefieres no responder preguntas? Modo no interactivo (ideal para agentes de IA):

```bash
./init-project.sh --name MiAppGenial --package com.empresa.miapp --app-name "Mi App Genial" --yes
```

> 📖 **Guía completa paso a paso:** [SETUP.md](./SETUP.md)

## 🤖 Desarrollo con IA

1. *(Opcional, recomendado)* Instala una vez las skills [hb-*](https://github.com/Hacybeyker/agent-skills) en tu máquina. Sin ellas, el agente trabaja igual con las reglas de `AGENTS.md`; con ellas suma el perfil de arquitectura completo y los flujos para implementar, revisar, hacer commits y versionar.
2. Abre tu agente (Claude Code, Copilot, Gemini en Android Studio, Junie…) en la raíz del proyecto y pídele lo que necesites:

> *"Implementa la feature de perfil de usuario."*

`hb-implement` lee `AGENTS.md`, aplica el perfil `hb-stack-mobile-kotlin` y corre el DoD del proyecto; `hb-review` revisa, `hb-commit` prepara los commits y `hb-release` corta las versiones.

## 🏗️ Estructura del proyecto

```
.
├── app/src/main/java/<package>/
│   ├── core/              # Compartido SOLO si ≥2 features lo necesitan
│   │   └── designsystem/  # Design tokens (Color, Type, Shape, Spacing, Theme)
│   ├── navigation/        # AppNavDisplay (Navigation 3): arma el back stack con las entries de cada feature
│   └── feature/home/      # Feature de EJEMPLO, completa
│       ├── domain/        #   model/ + repository/ (contrato) + usecase/ (Kotlin puro)
│       ├── data/          #   repository/: InMemoryGreetingRepository
│       ├── ui/            #   Screen/Content + ViewModel + UiState/Action + NavKey + Navigation (entry builder)
│       └── di/            #   HomeModule (Hilt, @Binds)
├── AGENTS.md              # Reglas del proyecto para agentes de IA
├── CLAUDE.md              # Importa AGENTS.md para Claude Code
├── init-project.sh        # ⚡ Inicializador del scaffolding
└── SETUP.md               # Guía detallada de inicialización
```

> La feature `home` es el ejemplo vivo de la arquitectura (con sus tests). `init-project.sh` la reemplaza por una pantalla mínima para que tu proyecto arranque limpio.

## 🔨 Comandos útiles

```bash
# Compilar la app
./gradlew assembleDebug

# Tests unitarios + screenshots (JVM — sin emulador)
./gradlew test

# Calidad de código (ktlint + detekt + Android Lint)
./gradlew formatAndAnalyze     # formatea, verifica y compila los tests
./gradlew codeQuality          # solo verifica (ideal para CI)

# Reportes HTML navegables (imprime las rutas file:// al terminar)
./gradlew qualityReports --continue

# Gates
./gradlew koverVerifyDebug        # cobertura (>=90% de domain/data/ViewModels)
./gradlew verifyRoborazziDebug    # screenshots contra los goldens del repo
./gradlew recordRoborazziDebug    # regenera goldens tras un cambio visual intencional
./gradlew sonar                   # Sonar (requiere SONAR_TOKEN)
```

> El CI encadena `test koverVerifyDebug verifyRoborazziDebug` en una sola invocación: los tests corren
> **una vez** y los tres gates reutilizan esa corrida.

### 📊 Reportes

Cada herramienta emite dos formatos: uno HTML para leerlo y uno XML para que lo ingiera Sonar (u otra
plataforma). Todos cuelgan de `app/build/reports/`.

| Herramienta | HTML (para leer) | XML (para Sonar) |
|-------------|------------------|------------------|
| Android Lint | `lint-results-debug.html` | `lint-results-debug.xml` (+ `.sarif`) |
| detekt | `detekt/detekt.html` | `detekt/detekt.xml` (+ `.sarif`, `.md`) |
| ktlint | `ktlint/<sourceSet>/*.html` | `ktlint/<sourceSet>/*.xml` (Checkstyle) |
| Kover | `kover/htmlDebug/index.html` | `kover/reportDebug.xml` (JaCoCo) |
| Tests | `tests/testDebugUnitTest/index.html` | `build/test-results/testDebugUnitTest/*.xml` (JUnit) |
| Roborazzi | `roborazzi/debug/index.html` | — |

`qualityReports` corre las cuatro herramientas y lista las rutas. Usa `--continue` para que se
generen los cuatro reportes aunque uno de los gates falle, que es justo cuando quieres leerlos.

### SonarQube / SonarCloud (opcional)

**El análisis es opt-in.** El paso del CI está guardado por `if: env.SONAR_TOKEN != ''`, así que un
repo sin el secret —un fork, un PR externo, o este mismo scaffolding, que no tiene proyecto en Sonar—
no intenta subir nada y el CI queda verde. No hay que desactivar nada.

Para activarlo: impórtalo en [sonarcloud.io](https://sonarcloud.io), **desactiva Automatic Analysis**
(choca con el scanner de Gradle) y añade el secret `SONAR_TOKEN`. Para una instancia self-hosted,
define además la variable de repo `SONAR_HOST_URL`.

## 📄 Licencia

Usa esta plantilla libremente para cualquier proyecto, personal o comercial. `init-project.sh` genera el `LICENSE` de tu proyecto (MIT por defecto, o ninguno con `--license none`).

---

Hecho con ❤️ para acelerar el desarrollo **Android + IA**. Si te sirve, ¡deja una ⭐ en el repo!
