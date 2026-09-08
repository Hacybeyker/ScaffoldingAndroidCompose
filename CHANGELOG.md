# Changelog — ScaffoldingAndroidCompose

> Todos los cambios notables de este proyecto están documentados aquí.
> Formato basado en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/).

---

## [Unreleased]

### ✨ Added
- **Flag `--github-user` (`-g`) en `init-project.sh`**, también pregunta en modo interactivo. Alimenta los badges del README, los links del CHANGELOG y, por defecto, `sonar.organization`. Antes solo se aceptaba como variable de entorno `GITHUB_USER`, sin equivalente en flags, así que era fácil terminar con badges apuntando a `TU_USUARIO`.
- **Aviso de placeholders pendientes** al final del init: lista `TU_USUARIO` y `TU_ORG_SONAR` cuando quedan sin rellenar, en vez de dejarlos escondidos en un TODO del README.
- **Tarea `qualityReports`**: genera el HTML de Android Lint, detekt, ktlint y Kover en una sola invocación e imprime las rutas `file://` al terminar. Con `--continue` los cuatro se producen aunque un gate falle.
- **Reporte HTML de ktlint**: el `ReporterType.HTML` faltaba, así que ktlint era la única de las cuatro herramientas sin salida navegable.
- **`sonar.junit.reportPaths`**: sin él, el dashboard de Sonar mostraba 0 tests ejecutados.
- **`app/src/test/resources/robolectric.properties`**: fija el SDK de Robolectric, que si no toma el `targetSdk` del módulo y falla en cuanto este supera la última API que Robolectric soporta.
- **Goldens de Roborazzi de `HomeContent`** (light y dark) versionados en `app/src/test/screenshots/`. El test existía pero su línea base no estaba commiteada, así que `verifyRoborazziDebug` fallaba en cualquier máquina que no fuera la que lo escribió.
- **Gate de cobertura con Kover**: mide `domain`, `data` y `*ViewModel*`; excluye codegen (Hilt/Room) y la frontera de plataforma. Umbral de línea al 90%.
- **SonarCloud**: plugin en la raíz alimentado por los reportes de Android Lint, detekt, ktlint y Kover, con `sonar.qualitygate.wait` para que el CI falle si el Quality Gate falla. La organización se configura con `./init-project.sh --sonar-org`.
- **Screenshot testing con Roborazzi + Robolectric**: goldens versionados en `app/src/test/screenshots/`, con `verifyRoborazziDebug` como gate y un golden de ejemplo de `HomeContent` (light y dark).
- Tests de ejemplo que faltaban para cubrir el slice completo: `ObserveGreetingUseCaseTest` e `InMemoryGreetingRepositoryTest`.
- `build-scan-publish` en CI: cada run publica un Build Scan para diagnosticar fallos sin repetirlos.
- Skill `code-reviewer`: revisión de código contra los estándares de `AGENTS.md` (arquitectura VSA + Clean + MVI, coding standards, testing, seguridad) con verificación automatizada del DoD y reporte por severidad.

### ♻️ Changed
- **`targetSdk` 36 → 37**, a la par del `compileSdk`: por debajo, Android aplica modos de compatibilidad y Lint reporta `OldTargetApi`. Los tests JVM se fijan a la API 36 vía `robolectric.properties` porque Robolectric aún no trae la imagen de la 37.
- **`init-project.sh` reordena los imports** de todos los `.kt` tras el rename de package. El rename es un `sed` en sitio, así que el import del proyecto se quedaba en la posición del scaffolding: con un package como `ai.startup.nova` —que ordena antes de `androidx`— el primer `./gradlew codeQuality` del proyecto nuevo fallaba con `standard:import-ordering`.
- **Badges del README generado** derivados de `libs.versions.toml` y del build en vez de hardcodeados (ya anunciaban Kotlin 2.4.0 y Compose BOM 2026.06 con el catálogo en 2.4.10 y 2026.08.00).
- **`--sonar-org` ya no cae por defecto en `hacybeyker`**: un `./init-project.sh` sin esa flag grababa la organización del autor de la plantilla en el proyecto de quien la usara. Ahora deriva de `--github-user` (SonarCloud crea la organización con la clave de la de GitHub) y, si no hay ninguno, deja el placeholder `TU_ORG_SONAR`.
- **El link `[Unreleased]` del CHANGELOG generado** usa el usuario de GitHub real en vez del literal `TU_ORG`, que ni siquiera coincidía con el `TU_USUARIO` del README.
- **`sonar.host.url`** se lee de `SONAR_HOST_URL` con SonarCloud como default, para poder apuntar a una instancia self-hosted sin editar el build.
- **Pre-commit hook**: re-stagea solo los archivos Kotlin que ya formaban parte del commit. Antes hacía `git add` sobre todo lo modificado, arrastrando al commit el trabajo en curso que la persona había dejado fuera a propósito.
- **R8 activo en release** (`optimization { enable = true }`) y **`lint.abortOnError = true`**: los gates ahora pueden fallar de verdad.
- `versionName` se lee de `appVersion` en el Version Catalog, que es también la fuente de `sonar.projectVersion`.
- ktlint publica los reportes `PLAIN`, `HTML` y `CHECKSTYLE` (este último es el único que Sonar sabe leer).
- CI: actions **pineadas al SHA** del commit (una etiqueta `@vN` es mutable), `fetch-depth: 0` para el blame de Sonar, y los reportes se suben con `if: always()` en vez de solo al fallar.
- `release.yml` corre los mismos gates de cobertura y goldens antes de publicar artefactos.
- `AGENTS.md`: reglas duras nuevas (no ejecutar en emulador/dispositivo, no commitear por cuenta propia, no bajar un gate para que pase el build, naming sin sufijo `Impl`).
- `compose_stability.conf` marca `androidx.lifecycle.ViewModel` como estable para mejorar la *skippability* de los composables.

### 🗑️ Removed
- `res/values/colors.xml`: los siete colores del template de Android Studio no los usaba nadie (el tema Compose vive en `core/ui/theme/Color.kt`) y generaban siete warnings de `UnusedResources` en cada build.
- `android:label` redundante en la `<activity>` del manifest, que ya heredaba el de `<application>`.
- Source set `androidTest` y la dependencia de Espresso: toda la verificación automatizada corre en la JVM (unitarios + Robolectric/Roborazzi) y lo que depende de hardware se prueba a mano en un dispositivo.
- Scaffolding inicial para proyectos Android nativos con Jetpack Compose:
  - Arquitectura Vertical Slice + Clean + MVI con feature de ejemplo (`feature/home`) y tests (JUnit + Turbine + Fakes).
  - Hilt (DI por feature) y Navigation 3 (NavKeys type-safe).
  - Design tokens Material 3 (`core/ui/theme/`: Color, Type, Shape, Spacing).
  - Calidad de código: ktlint + detekt + Android Lint con tareas `codeQuality` y `formatAndAnalyze`.
  - Infraestructura de IA: `AGENTS.md` + `.agents/` (skills `android-best-practices`, `feature-implementation`, `git-commit`, `changelog-generator`, `skill-creator`, `skill-linker`) con symlinks multi-IDE.
  - `init-project.sh`: inicializador que renombra proyecto/package, limpia el ejemplo y configura git.
  - CI/CD: GitHub Actions (`ci.yml`, `release.yml`) + Dependabot.
  - Pre-commit hook de calidad (`scripts/setup-quality-hook.sh`).

[Unreleased]: https://github.com/hacybeyker/ScaffoldingAndroidCompose/commits/main
