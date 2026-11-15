# Mixi Framework Manager

UI for MixiHost built with Kotlin, Jetpack Compose and **Miuix by YuKongA**, pinned to 0.9.4. Android15+ / targetAndroid17. Milestone `0.9.0-host-ui`: one installed `com.mixi.host` APK; the standalone `com.mixi.manager` build remains a development preview only.

## Implemented

- HyperOS-style large titles, grouped cards, Miuix floating blurred navigation and back buttons.
- Progressive TopAppBar blur from the original Miuix effects, content drawn behind bars, transparent gesture navigation with contrast scrim disabled.
- Compact honest connection-state card; original Miuix NavDisplay detail navigation animates complete pages, and root tabs use a neighbor-retaining HorizontalPager.
- Floating-tab motion uses Miuix Folme spring instead of a fixed-duration tween; rapid retargeting still settles at the selected tab.
- Catalog metadata is generated into Kotlin before build, avoiding JSON asset parsing on startup; page groups are indexed once. No branded splash/logo animation or separate splash Activity. Day/night themes disable the starting-window preview and use a transparent icon fallback; a cold start still waits for Android and the first rendered app frame.
- Release builds enable R8 and resource shrinking. Use the optimized release preview for performance evaluation; it is signed with the same debug key for easy preview upgrades, not a production security identity.
- Home ROM/device/Android/kernel facts, feature shortcuts, honest disconnected state.
- Shared dropdown menus reuse Miuix popup motion/positioning and radio/check rows with a 28dp rounded translucent blur surface, no full-screen dim, adaptive height, intrinsic content width capped at240dp, and dismiss-on-select/outside/back. Popup texture noise is disabled; item press highlights fill their rows, with uniform row spacing and an8dp inset on all four sides. First/last rows use the same vertical padding as middle rows.
- Searchable92-field catalog across10groups: HMC Enhanced subpages, Soft Glass and extensions.
- Original Miuix switches, overlay dropdowns and sliders with live numbers; slider previews persist on release.
- System/light/dark themes and preserved page scroll; Mixi logo, about and source credits.

## Soft Glass preview

The Soft Glass page includes a static locally drawn scene and a Miuix sample panel. The sample sits immediately below the master switch; all blur/background/opacity/reflection sliders follow underneath. Slider drafts update the illustration during dragging, while release sends configuration through Settings in the Host build (or saves local preferences in the standalone preview). The master switch returns the sample to a neutral panel when off. There is no continuous wallpaper animation or extra network/image dependency.

This preview is deliberately a **UI illustration**, not the HyperOS material renderer. Parameters are mapped for visual feedback only and do not promise pixel parity with notifications/Control Center. Exact preview material parity remains deferred. In the Host build, slider release sends the real setting through the existing Settings configuration protocol; the illustration itself does not become an OEM renderer.

## HyperImmersive and Island UI

HyperImmersive has its own page with version/status from its bridge when available, StandBy and blurred-background switches, numeric idle/off fields, seconds/minutes menus anchored to the unit button, and a zero-time warning. Host mode uses the existing ImmersiveClient configuration and preview calls through Settings. Standalone preview remains local and disconnected.

Lockscreen SuperIsland uses the original HMC-Enhanced IslandDemo WebP assets, unchanged: choosing ColorOS or HyperOS switches the correct animation. The upstream ImageDecoder/ImageView approach is retained, with decoding off the UI thread and lifecycle-aware start/stop. Assets and source attribution are recorded in NOTICE.md; this is UI demo reuse, not a feature-runtime port.

## Boundary

The shipped test is one Host APK inside a reversible module. Its Activity runs in `:manager`; original API/HMC DEX, resources, assets and libraries are preserved. The UI is an embedded, checksum-verified private module with resource package `0x80`, loaded with a boot-parent classloader and ResourcesLoader only when opened. There is no second installed Manager APK or launcher icon.

The Settings header opens that Activity and lazily registers a relay restricted to the actual Host sender UID/package. Settings keeps its existing UID1000 authority, catalog validation, pending/replay logic, Host provider and HMC/Immersive protocols. Runtime caller checks are unchanged. Existing Settings configuration is read; preview-Manager preferences are never imported. A separate bounded request queue has an8-second reply deadline; original runtime drains do not hold foreground broadcasts open. Appearance acknowledgement is guarded against losing a newer concurrent edit.

Connection to Settings and an HMC probe response are reported separately. Saved/pending does not claim every feature is active. No polling loop, boot receiver, service, root request or new native library is added. If Settings is unavailable, open the UI again from its Settings entry. Physical launch, resource loading, live configuration and reboot acceptance are still pending; this is not a ROM release.

## Build

Use JDK21 and AndroidSDK37/build-tools37.0.0. Configure SDK with Android Studio or ignored local.properties, then run:

```
gradlew.bat :app:assembleRelease :app:lintRelease
python scripts/generate_catalog.py
python scripts/verify_catalog.py
```

Output: `app/build/outputs/apk/release/app-release.apk`. This is a debug-signed preview, not a production signing configuration. Never commit signing keys or local.properties.

`catalog.json` was exported from Mixi Framework1.0.4 EnhancedCatalog (92controls) using the original Java initializer, preserving keys/defaults/options. Run the generator after modifying the source catalog; the verifier checks generated metadata stays in sync. The repository verifier checks the shipped data and UI-only app-manifest boundary. `VERIFICATION.json` records build, lint, signature and payload checks. Visual approval on a physical phone is still pending; build/lint do not establish pixel-perfect HyperOS parity.

## Host test assembly

Requires this checkout at `MixiFrameWork/manager/MixiFramework-Manager`, the existing Framework1.0.4 workspace fixtures and its local signing key. `host-integration/framework-inputs.json` pins external source inputs and the packager checks the accepted Host hash. No key or APK is stored in Git.

```
gradlew.bat -PhostUi=true :app:assembleRelease :app:lintRelease
python host-integration/build_host.py
python host-integration/test_integration.py
python host-integration/package_test.py
```

Install only the resulting `Mixi-v1.0.4-HostUI-0.9.0-Test.zip` through the module manager, then reboot manually. Do not install its Host APK separately. The ZIP keeps the accepted installer and external runtime files; see its Vietnamese guide. Rollback: the accepted `Mixi-v1.0.4-Native-Test.zip`, with no app-data deletion. Source rollback before integration is `8c4bfe5`; integration is `16bba7d`.

## Credits

[Miuix](https://github.com/compose-miuix-ui/miuix) — YuKongA and contributors, Apache-2.0. HMC/Enhanced — zyl6932/Lewewe, AGPL-3.0. HyperModifier — AritxOnly. See LICENSE/NOTICE and bundled attribution files. Original feature APKs are not committed in this UI repository; the test assembly retains the accepted Host payload unchanged.

## 0.9.1 preview test
Current test adds a green connection check and removes routine saved/runtime notes. The preview now attempts the device wallpaper plus OEM Glass using the original HyperModifier recipe. It no longer draws the sample landscape or Miuix approximation. Wallpaper/helper/API unavailability is shown rather than silently substituting another renderer. Device validation remains required. Artifact: Mixi-v1.0.4-HostUI-0.9.1-Test.zip; previous0.9.0 ZIP remains available for rollback.
