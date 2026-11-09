# Changelog — ScaffoldingAndroidCompose

> Todos los cambios notables de este proyecto están documentados aquí.
> Formato basado en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/).  
> El template se versiona solo en este archivo: no se crean tags ni GitHub Releases.

---

## [Unreleased]

### Added
- Scaffolding inicial para proyectos Android nativos con Jetpack Compose:
  - Clean Architecture + MVVM con UDF por features (perfil `hb-stack-mobile-kotlin`), con feature de ejemplo (`feature/home`) y tests (JUnit + Turbine + Fakes).
  - Hilt (DI por feature) y Navigation 3 (NavKeys type-safe).
  - Design tokens Material 3 (`core/designsystem/`: Color, Type, Shape, Spacing).
  - Calidad de código: ktlint + detekt + Android Lint con tareas `codeQuality` y `formatAndAnalyze`.
  - Infraestructura de IA: `AGENTS.md` (estándar [agents.md](https://agents.md/)) y `CLAUDE.md`, que lo importa. Las skills son globales ([hb-*](https://github.com/Hacybeyker/agent-skills)) y no se copian al proyecto.
  - `init-project.sh`: inicializador que renombra proyecto/package, limpia el ejemplo y configura git.
  - CI/CD: GitHub Actions (`ci.yml`, `release.yml`) + Dependabot.
  - Pre-commit hook de calidad (`scripts/setup-quality-hook.sh`).
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

### Changed
- `init-project.sh --help` lee el encabezado completo en vez de un rango fijo de líneas, que deja de mostrar la ayuda entera en cuanto esta crece.
- El template se versiona solo en `CHANGELOG.md`, sin tags ni GitHub Releases. La regla está en `AGENTS.md` y `init-project.sh` la quita en los proyectos nuevos, que sí publican versiones con tags; también quita la nota sobre la feature de ejemplo, que en el proyecto nuevo queda como una pantalla mínima. README y SETUP marcan las skills hb-* como opcionales.
- **CI y release:** los runners pasan de `ubuntu-latest` a `ubuntu-26.04` fijo, `setup-java` y `gradle/actions` suben a su última versión (fijadas por SHA) y el Build Scan se publica solo si el repo es público: los scans van a la instancia pública de Gradle y no heredan la privacidad del repo.
- **Encabezados estándar de Keep a Changelog** (`Added`, `Changed`, `Removed`, `Fixed`, sin emoji y en ese orden), también en el CHANGELOG que genera `init-project.sh`: es el formato que escriben las skills `hb-*`.
- **Estructura alineada con el perfil `hb-stack-mobile-kotlin`** sin cambiar el comportamiento ni el aspecto de la app: tokens en `core/designsystem/`; `feature/home` con `domain/model`, `domain/repository`, `data/repository` y el módulo Hilt en `di/HomeModule`; contrato de presentación `HomeUiState` + `HomeAction` con un único `onAction`; `HomeContent` sin estado en su propio archivo; cada feature declara su NavKey en `ui/<Feature>NavKey.kt` y su entry builder en `ui/<Feature>Navigation.kt`, y `navigation/AppNavDisplay.kt` solo arma el back stack; `MainDispatcherRule` en `core/testing`.
- **`targetSdk` 36 → 37**, a la par del `compileSdk`: por debajo, Android aplica modos de compatibilidad y Lint reporta `OldTargetApi`. Los tests JVM corren en la misma API 37, fijada en `robolectric.properties`.
- **Dependencias al día:** AGP 9.4.0 → 9.5.0, Gradle 9.7.1 → 9.8.2 (wrapper regenerado con `./gradlew wrapper` y con `distributionSha256Sum`, que verifica la distribución descargada), Kotlin 2.4.10 → 2.4.21, KSP 2.3.11 → 2.3.13, Compose BOM 2026.08.00 → 2026.09.00, Navigation 3 1.1.7 → 1.2.0, kotlinx.serialization 1.11.0 → 1.12.0, core-ktx 1.19.0 → 1.19.1, Robolectric 4.16.1 → 4.17, Roborazzi 1.73.0 → 1.77.0 y Kover 0.9.9 → 0.9.11.
- **Tests JVM con los `--add-opens` que pide Robolectric 4.17 en JDK 17+** ([guía oficial](https://robolectric.org/getting-started/)): sin `jdk.internal.access`, los screenshot tests fallaban al arrancar el entorno de SDK 36 (`ApplicationSharedMemory.create()`).
- **Tests JVM en la API 37** (antes 36), la misma que el `targetSdk`. Necesita Robolectric 4.17 y **`espresso-core` 3.7.0 como `testImplementation`**. Esto no reintroduce tests de Espresso (sigue sin haber `androidTest`): `compose-ui-test` usa Espresso por dentro con Robolectric, y la 3.5.1 que traía transitiva falla en la API 37 porque llama a `InputManager.getInstance()`, que ya no existe. Los goldens no cambian.
- **`AGENTS.md`: cómo subir Gradle** con `./gradlew wrapper --gradle-version … --gradle-distribution-sha256-sum …` y por qué no se edita `gradle-wrapper.properties` a mano.
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

### Removed
- `res/values/colors.xml`: los siete colores del template de Android Studio no los usaba nadie (el tema Compose vive en `core/designsystem/Color.kt`) y generaban siete warnings de `UnusedResources` en cada build.
- `android:label` redundante en la `<activity>` del manifest, que ya heredaba el de `<application>`.
- Source set `androidTest` y sus tests de Espresso (`espresso-core` vuelve solo como dependencia interna de los tests JVM; ver Changed): toda la verificación automatizada corre en la JVM (unitarios + Robolectric/Roborazzi) y lo que depende de hardware se prueba a mano en un dispositivo.

### Fixed
- **`formatAndAnalyze` converge en una sola invocación.** Los `ktlint*SourceSetCheck` fallaban con violaciones que `ktlintFormat` ya había corregido en esa misma corrida, porque el `mustRunAfter` estaba puesto solo sobre el agregado `ktlintCheck` y sus subtareas arrancaban antes que el formato. Ahora el orden se declara sobre cada check por source set.
- **El pre-commit hook avisa del formato solo si cambió algo.** Imprimía "corrigió el formato" en cualquier commit con archivos `.kt` o `.kts`, hubiera corregido algo o no, y mandaba a revisar un diff que no existía. Ahora compara el contenido de los archivos Kotlin del commit antes y después de `formatAndAnalyze`: lista y re-stagea solo los que cambió, y no dice nada si no cambió ninguno.
- **`detekt.yml` usa las claves de detekt 2.0.** Con `config.validation: false`, las claves de la 1.23 (`thresholdInFiles`, `thresholdInClasses`, `threshold` de `LongMethod`, `functionThreshold` y `constructorThreshold` de `LongParameterList`) se ignoraban sin avisar y las reglas aplicaban sus umbrales por defecto (11 funciones, 60 líneas). Ahora son `allowedFunctionsPerFile`, `allowedFunctionsPerClass`, `allowedLines`, `allowedFunctionParameters` y `allowedConstructorParameters`, con los valores que el archivo ya declaraba (30, 30, 80, 8 y 8): **el umbral efectivo de `TooManyFunctions` pasa de 11 a 30 y el de `LongMethod` de 60 a 80**. `LongParameterList` exige resolución de tipos y no corre en la tarea `detekt`, solo en `detektMain` o `detektDebug`.
- Un nombre visible con `&`, `|`, `\`, comillas o un `@`/`?` al inicio rompía el reemplazo de `init-project.sh` o el `strings.xml`: ahora se escapa para sed, XML y aapt2.
- `init-project.sh` valida el usuario de GitHub con las reglas de GitHub, y el aviso final ya no sugiere volver a correr el script, que no se puede ejecutar dos veces.

[Unreleased]: https://github.com/Hacybeyker/ScaffoldingAndroidCompose/commits/main
