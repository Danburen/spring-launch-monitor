# spring-launch-monitor

A plug-and-play Spring Boot startup monitor that captures startup phases and bean initialization timing, then outputs a readable startup report plus flame-tree artifacts.

## What It Provides

- Startup timeline capture (from early boot phases to ready).
- Bean initialization timing collection.
- Console startup report (plain text).
- Optional flame folded output to console.
- HTML flame tree report for visual diagnosis.
- Compact JSON tree output for custom dashboards/tools.

## Principle

- Early startup events are captured through Spring Boot lifecycle listeners (for example starting/environment/context/ready phases).
- Bean initialization timing is collected through `BeanPostProcessor` hooks and aggregated as startup records.
- Data is split into two output lanes:
  - Console: lightweight plain-text report (stable and charset-friendly).
  - Artifacts: HTML (rich i18n report + flame tree) and JSON (compact package tree for tooling).
- Package-level flame tree is built by aggregating bean class package segments (for example `org -> springframework -> ...`) with cumulative latency.

## How To Use

### 1) Add Dependency

Use your published coordinate (example placeholder below):

```groovy
// SNAPSHOT placeholder (recommended for docs/examples)
def springLaunchMonitorVersion = '0.0.1-SNAPSHOT'

dependencies {
    implementation "io.github.danburen:spring-launch-monitor:${springLaunchMonitorVersion}"
}
```

If you publish locally first, see `How To Build` and `Use From Another Local Project`.

### 2) Add Configuration (Optional)

```yaml
launch:
  monitor:
    enable: true
    report: true
    flame:
      console: false
      html: true
      json: true
    output-dir: build/reports/spring-launch-monitor
    locale: auto
```

### 3) Start Application

At `ApplicationReadyEvent`, the monitor will:

- Print console report (when `report=true`).
- Print folded flame output (when `flame.console=true`).
- Write artifacts (depending on `flame.html/json`):
  - `flame-tree.html`
  - `flame-tree.json`

## Configuration Reference

- `launch.monitor.enable`
  - Master switch; default `true`.
- `launch.monitor.report`
  - Console startup report switch; default `true`.
- `launch.monitor.flame.console`
  - Console folded flame output; default `false`.
- `launch.monitor.flame.html`
  - Write HTML flame tree artifact; default `true`.
- `launch.monitor.flame.json`
  - Write JSON flame tree artifact; default `true`.
- `launch.monitor.output-dir`
  - Output directory for generated artifacts.
- `launch.monitor.locale`
  - i18n locale for HTML/report labels, e.g. `auto`, `en-US`, `zh-CN`.

### Legacy Compatibility Keys

Currently still recognized for compatibility:

- `launch.monitor.report.enabled`
- `launch.monitor.flame.enabled`

Prefer new keys under `launch.monitor.flame.*`.

## Output Files

By default, files are generated under:

`build/reports/spring-launch-monitor`

- `flame-tree.html`
  - Rich report + package flame tree visualization.
- `flame-tree.json`
  - Compact package tree JSON.

## How To Build

### Build Library

```powershell
./gradlew.bat clean build
```

### Run Tests

```powershell
./gradlew.bat test
```

### Publish To Local Maven

```powershell
./gradlew.bat publishToMavenLocal
```

## Use From Another Local Project

1. Ensure your consumer project includes `mavenLocal()`:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}
```

2. Add dependency:

```groovy
// SNAPSHOT placeholder
def springLaunchMonitorVersion = '0.0.1-SNAPSHOT'

dependencies {
    implementation "io.github.danburen:spring-launch-monitor:${springLaunchMonitorVersion}"
}
```

3. Refresh dependencies:

```powershell
./gradlew.bat --refresh-dependencies build
```

## QA

- Keep this section reserved for QA checklists and known test baselines.
- Temporary placeholder (do not remove for now):
  - [ ] Startup phase timeline baseline
  - [ ] Bean slow-top accuracy check
  - [ ] HTML/JSON artifact generation check
  - [ ] i18n locale rendering check

## Troubleshooting

### IDE says "Cannot resolve configuration property"

- Ensure you are using the latest built/published jar.
- Refresh Gradle project in IDE.
- Invalidate IDE caches if needed.
- Use canonical kebab-case keys (for example `output-dir`, not `outputDir`).

### YAML loading fails due to `snakeyaml` missing

- Ensure dependency graph still includes YAML parser at runtime.
- Avoid excluding required transitive dependencies accidentally.

## License

See `LICENSE`.
