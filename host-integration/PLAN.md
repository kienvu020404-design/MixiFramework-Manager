# MixiHost UI integration

Baseline rollback: Manager main 8c4bfe5; accepted Host 1.0.4 from out/v104/MixiHost.apk.

- Ship one installed com.mixi.host APK. Retain original Host DEX/resources/native libraries and feature metadata.
- Embed the release UI as a private module. Load it only in :manager with an isolated classloader and ResourcesLoader; never add Compose to SystemUI or Settings classloaders.
- Settings header opens Host Activity. Settings-owned configuration relay validates the real Host sender UID and reuses EnhancedCatalog, EnhancedProtocol, MasterProvider and ImmersiveClient. Runtime authentication stays unchanged.
- Read existing Settings configuration and Host master/Glass state. Never replay preview-manager preferences into runtime. Explicit connection/error status; no periodic polling, boot receiver or new service.
- Commit packaging, authenticated config/entry and UI wiring separately. Preserve rollback artifact; no automatic install/reboot.
- Before handoff: release/lint, signature pairing, DEX preservation, embedded-module checksum, catalog validation and package integrity. Device acceptance must verify Settings launch, configuration persistence/live Glass, lifecycle and accepted HMC behavior; build checks do not prove those.

The existing Settings process is the authority for configuration. If it is unavailable, show an error rather than claim an apply succeeded. This integration does not make the Host system UID or relax runtime caller checks.

Completed local stage2026-11-16: release/lint, 92-field validation, extracted concurrent-ACK guard check, signature identity, resource-package isolation, original DEX/resource preservation and final ZIP checks PASS. Source checkpoint16bba7d. Device acceptance remains deferred by user. Only SettingsExtension-owned core classes and the settings module change; factory is appended separately. No changes to HMC/SystemUI/system_server hook implementation.
