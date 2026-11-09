# {{PROJECT_NAME}}

App Android nativa con Jetpack Compose, generada desde ScaffoldingAndroidCompose.

## Stack

- Perfil: hb-stack-mobile-kotlin
- Variante: android
- Package base: `{{PACKAGE_NAME}}`
- Versiones: **siempre** en `gradle/libs.versions.toml`; no las asumas.
- Calidad: ktlint, detekt y Android Lint; gate de cobertura con Kover (90 %); goldens de UI con Roborazzi; SonarCloud en CI.

## Comandos

| Para | Comando |
| --- | --- |
| Compilar | `./gradlew assembleDebug` |
| Tests unitarios y de screenshots (JVM) | `./gradlew test` |
| Formatear, analizar y compilar los tests | `./gradlew formatAndAnalyze` |
| Regrabar goldens tras un cambio visual intencional | `./gradlew recordRoborazziDebug` |
| Reportes HTML de Lint, detekt, ktlint y Kover | `./gradlew qualityReports --continue` |
| Hook de calidad (una vez, tras clonar) | `./scripts/setup-quality-hook.sh` |
| Subir Gradle (dos veces; ver Reglas) | `./gradlew wrapper --gradle-version X.Y.Z --gradle-distribution-sha256-sum <sha256>` |

## DoD

Antes de dar una tarea por terminada, en este orden:

1. `./gradlew formatAndAnalyze`
2. `./gradlew test`
3. `./gradlew koverVerifyDebug`
4. `./gradlew verifyRoborazziDebug`
5. `./gradlew assembleDebug`

## Arquitectura

Sigue el perfil (Clean Architecture + MVVM con UDF, por features). Particularidades de este proyecto:

- Un solo módulo, `{{MODULE_NAME}}`, con los paquetes `core/`, `navigation/` y `feature/<nombre>/`. Los design tokens están en `core/designsystem/`.
- **La calidad se configura una vez, en `build-logic/`, con convention plugins** (el patrón de Now in Android): ktlint, detekt, Lint, Kover y Roborazzi no se declaran en los módulos. Cada módulo los recibe con `alias(libs.plugins.convention.android.application)` o `alias(libs.plugins.convention.android.library)`, que aplican el plugin de Android correspondiente y los gates; los comandos del DoD corren desde la raíz sobre todos los módulos. Si hace falta una opción nueva común, va en `build-logic/src/main/kotlin/convention/`, en `AndroidApplicationConventionPlugin` **y** en `AndroidLibraryConventionPlugin` (en AGP 9, `CommonExtension` no expone `testOptions`). ktlint revisa también los scripts del raíz (`build.gradle.kts`, `settings.gradle.kts`) y `build-logic/`, que entran en `formatAndAnalyze` y `codeQuality`; detekt no se aplica a `build-logic/`.
- **El build no lee ni modifica otros proyectos** (`allprojects`, `subprojects {}`, `rootProject.tasks`), para seguir siendo compatible con la caché de configuración y con Isolated Projects. Lo cumple todo el build propio; hoy Isolated Projects no se puede activar porque el plugin `org.sonarqube` usa `allprojects`.
- **Un módulo sin gates no pasa desapercibido.** `settings.gradle.kts` hace fallar la configuración de cualquier módulo que aplique `com.android.*` o un plugin de Kotlin (`kotlin("jvm")`, `com.android.test`, Kotlin Multiplatform) sin `convention.android.application` o `convention.android.library`. Para los tipos de módulo que no son application ni library la plantilla todavía no trae gates: o se escribe su convención en `build-logic/`, o se deja sin gates a propósito con `alias(libs.plugins.convention.quality.exempt)`.
- **Sumar un módulo library** (por ejemplo `:core-data`), sin tocar la configuración de calidad:
  1. `include(":core-data")` en `settings.gradle.kts`.
  2. `core-data/src/main/AndroidManifest.xml` con `<manifest />`.
  3. `core-data/build.gradle.kts` con `plugins { alias(libs.plugins.convention.android.library) }`, un bloque `android` (`namespace`, `compileSdk { version = release(37) }`, `defaultConfig { minSdk = 26 }` y `compileOptions` en Java 11) y sus dependencias, por ejemplo `testImplementation(libs.junit)`. Otros plugins (Hilt, Compose, KSP) solo si el módulo los usa.
  4. Quien lo use declara `implementation(project(":core-data"))`.
  5. Corre el DoD completo desde la raíz: debe quedar en verde con el módulo incluido.
  Kover mide al 90 % las clases `*.domain.*`, `*.data.*` y `*ViewModel*` de cada módulo: si el módulo las tiene, necesita sus tests.
- Cada feature declara su NavKey en `ui/<Feature>NavKey.kt` y su entry builder en `ui/<Feature>Navigation.kt`; `navigation/AppNavDisplay.kt` solo arma el back stack y junta los entry builders.
- `feature/home` es el ejemplo del template: `init-project.sh` lo reduce a una pantalla mínima (`HomeNavKey`, `homeEntry()` y `HomeScreen`).
- **Sin tests instrumentados:** no existe `androidTest`. El código que depende de hardware (cámara, GPS, biometría, sensores) se verifica a mano en un dispositivo y se excluye del gate de cobertura.
- `espresso-core` en `testImplementation` no contradice lo anterior: aquí no se escriben tests de Espresso. Lo usa `compose-ui-test` por dentro con Robolectric, y se declara explícito porque la versión que llega transitiva (3.5.1) falla en la API 37, que es la que emulan los tests (`app/src/test/resources/robolectric.properties`). Se puede quitar cuando `compose-ui-test` traiga la 3.7.0 o una superior por su cuenta.
- Los goldens de Roborazzi se versionan en `app/src/test/screenshots/`.

## Reglas del proyecto

- **Nunca sufijes una clase con `Impl`:** nómbrala por lo que la respalda (`RoomBookRepository`, `InMemoryGreetingRepository`). Los dobles de test se llaman `Fake…` o `Stub…`.
- **Una declaración pública por archivo, con el mismo nombre:** cada clase, objeto o interfaz pública vive en un archivo que se llama como ella (`HomeUiState` en `HomeUiState.kt`), y una segunda va en su propio archivo. detekt `MatchingDeclarationName` falla cuando el único tipo de un archivo se llama distinto que el archivo, por ejemplo tras renombrar uno de los dos.
- **Comentarios:** por defecto ninguno. Solo el porqué, una decisión o una advertencia; nunca lo que el nombre ya dice.
- Imports con comodín y trailing commas prohibidos (ktlint los rechaza en el build). Sin comentarios en línea en `libs.versions.toml`.
- **Gradle se sube con la tarea `wrapper`, nunca editando `gradle-wrapper.properties` a mano.** `distributionSha256Sum` verifica la distribución que se descarga: si solo cambia `distributionUrl`, el build falla por checksum. El sha256 es el de `gradle-X.Y.Z-bin.zip` (el wrapper usa la distribución `-bin`) y está en `https://downloads.gradle.org/distributions/gradle-X.Y.Z-bin.zip.sha256`. El comando se corre dos veces: la primera fija la versión nueva y la segunda, ya con ella, regenera `gradle-wrapper.jar`, `gradlew` y `gradlew.bat`. Si un PR de Dependabot sube Gradle, revisa que cambie la URL y el checksum juntos.
- **Manifiestos por buildType:** un `src/<buildType>/AndroidManifest.xml` se ignora si el módulo fija `manifest.srcFile` para ese source set, porque el `srcFile` lo reemplaza y no se suman. Si un manifest nuevo no llega al APK, revisa primero los `sourceSets` del módulo (`./gradlew processDebugMainManifest` y el manifiesto fusionado en `build/intermediates/merged_manifest/debug/`); para descartar la caché de configuración, repite con `--no-configuration-cache`.
- **No bajes un gate para que pase el build** (`abortOnError = false`, bajar `minBound`, `ignoreFailures`): arregla la causa.
- **No ejecutes la app** en un emulador ni en un dispositivo. La verificación automática llega hasta `assembleDebug` y `test`; lo que necesita hardware lo prueba la persona, así que indícale qué abrir y qué observar.
- Dependencias solo estables. Una excepción exige autorización explícita y queda documentada aquí con su contención:
  - **`detekt = 2.0.0-alpha.x`:** la rama 1.23.x no soporta Kotlin 2.4, así que la alternativa es quedarse sin detekt. Contención: `config/detekt/detekt.yml` corre con `config.validation: false` para tolerar renombres de reglas entre alphas, y `buildUponDefaultConfig = true` limita los overrides al archivo. Con la validación apagada, una clave que la alpha no conoce se ignora sin avisar y la regla aplica su umbral por defecto (las claves de la 1.23 como `thresholdInClasses` ya no hacen nada; en la 2.0 son `allowedFunctionsPerClass` y similares): al subir de alpha, comprueba cada umbral con un caso temporal. Vuelve a estable cuando detekt 2.0.0 sea GA.

## Git

- Commits: Conventional Commits, en español, sin emoji. Agrupados por unidad funcional: cada commit compila y pasa el DoD.
- Rama principal: `main`.
- **Flujo de integración (lo declara cada proyecto; cámbialo aquí si el tuyo es otro):** ramas cortas e integración por PR con squash. La alternativa es trabajar directo en `main`, sin ramas ni PR. Los agentes y las skills `hb-*` siguen lo que diga esta línea, así que corrígela antes de pedirles commits o PRs.
- **La IA no commitea:** deja los cambios sin commitear y entrega el mensaje. Tampoco hace `push`, `tag` ni `merge`.

## Versionado y release

- SemVer. La versión vive en `gradle/libs.versions.toml` (`appVersion`) y `versionCode`, en `app/build.gradle.kts`, sube de 1 en 1.
- Tags `vX.Y.Z`, creados por la persona. Al subir el tag, el workflow `release.yml` compila el APK y el AAB y crea el GitHub Release: no lo crees a mano.
- `CHANGELOG.md` con Keep a Changelog, en español: cada cambio entra en `[Unreleased]`.
- **Solo en el repo del template:** se versiona únicamente en `CHANGELOG.md`. No se crean tags ni GitHub Releases, y la versión de la app no se toca: es la inicial de los proyectos nuevos. `init-project.sh` borra esta línea.

## Seguridad

- Sigue el perfil (OWASP MASVS). Ningún secreto en el repo: van en `local.properties` o en los secretos de CI (por ejemplo `SONAR_TOKEN`).
