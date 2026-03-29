# spring-launch-monitor

A plug-and-play Spring Boot startup monitor that captures startup phases and bean initialization timing, then outputs a readable startup report plus flame-tree artifacts.

![GitHub Release](https://img.shields.io/github/v/release/Danburen/spring-launch-monitor?label=Latest%20Release)
![GitHub License](https://img.shields.io/github/license/danburen/spring-launch-monitor?label=License)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/danburen/spring-launch-monitor/total)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/io.github.danburen/spring-launch-monitor)
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.danburen/spring-launch-monitor)

## 📷 Screenshot

<details open>
  <summary>
     Expend/Collapse Screenshot
  </summary>
  Report
  <img width="1094" height="766" alt="image" src="https://github.com/user-attachments/assets/30dea6b2-50a4-4ea9-8833-7401e23598dd" />
  Package Flame Tree
  <img width="1848" height="805" alt="image" src="https://github.com/user-attachments/assets/5cd2145e-8838-40e2-9c01-a57a2644afdd" />

</details>

## 😄What It Provides

- Startup timeline capture (from early boot phases to ready).
- Bean initialization timing collection.
- Console startup report (plain text).
- Optional flame folded output to console.
- HTML flame tree report for visual diagnosis.
- Compact JSON tree output for custom dashboards/tools.

## ⚙️ Principle

- Early startup events are captured through Spring Boot lifecycle listeners (for example starting/environment/context/ready phases).
- Bean initialization timing is collected through `BeanPostProcessor` hooks and aggregated as startup records.
- Data is split into two output lanes:
  - Console: lightweight plain-text report (stable and charset-friendly).
  - Artifacts: HTML (rich i18n report + flame tree) and JSON (compact package tree for tooling).
- Package-level flame tree is built by aggregating bean class package segments (for example `org -> springframework -> ...`) with cumulative latency.

## 👉 How To Use

### 1) Add Dependency

Use your published coordinate (default example below). Replace `0.0.1` with the latest release from Maven Central before use:
```groovy
dependencies {
    implementation "io.github.danburen:spring-launch-monitor:0.0.1"
}
```
> [!IMPORTANT]
> Building Environment:
> 
> Java JDK 22
>   
> Groove Gradle: 9.4.0+
> 
> SpringBoot 4.0.4 

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

- Print console report (when `launch.monitor.flame.console=true`).
- Print folded flame output (when `flame.console=true`).
- Write artifacts (depending on `flame.html/json`):
  - `flame-tree.html`
  - `flame-tree.json`

## Configuration Reference

- `launch.monitor.enable` Master switch; default `true`.
- `launch.monitor.report` Console startup report switch; default `true`.
- `launch.monitor.flame.console` Console folded flame output; default `false`.
- `launch.monitor.flame.html` Write HTML flame tree artifact; default `true`.
- `launch.monitor.flame.json` Write JSON flame tree artifact; default `true`.
- `launch.monitor.output-dir` Output directory for generated artifacts.
- `launch.monitor.locale` i18n locale for HTML/report labels, e.g. `auto`, `en-US`, `zh-CN`.

### Legacy Compatibility Keys

Currently still recognized for compatibility:

- `launch.monitor.report.enabled`
- `launch.monitor.flame.enabled`

Prefer new keys under `launch.monitor.flame.*`.

## Output Files

By default, files are generated under:

`build/reports/spring-launch-monitor`

- `flame-tree.html` Rich report + package flame tree visualization.
- `flame-tree.json` Compact package tree JSON.

## 🧱 How To Build

### Build Library

```powershell
./gradlew.bat clean build
```

### Run Tests

```powershell
./gradlew.bat test
```

## License

See [LICENSE](https://github.com/Danburen/spring-launch-monitor/blob/main/LICENSE).
