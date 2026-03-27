# spring-launch-monitor
A plug-and-play Spring Boot startup monitor, auto-captures lifecycle events and bean init timing to build a startup timeline and diagnose slow startup fast.

## Behavior

- Auto outputs startup report when the app reaches `ApplicationReadyEvent`.
- Auto outputs flame graph folded data after the report.

## Configuration

```yaml
launch:
  monitor:
    enable: true
    report: true
    flame: true
    locale: auto
```

### Keys

- `launch.monitor.enable`: master switch, default `true`; set `false` to disable monitor.
- `launch.monitor.report`: enable/disable ready-time report output, default `true`.
- `launch.monitor.flame`: enable/disable ready-time flame output, default `true`.
- `launch.monitor.locale`: message locale (`auto`, `en-US`, `zh-CN`).

Legacy compatibility keys for output switches:

- `launch.monitor.report.enabled`
- `launch.monitor.flame.enabled`

