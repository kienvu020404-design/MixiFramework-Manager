# Mixi Framework Manager

Standalone Android UI built with Kotlin, Jetpack Compose and **Miuix by YuKongA**, pinned to 0.9.4. Android15+ / targetAndroid17. Package `com.mixi.manager`, UI milestone `0.8.3-ui`.

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

The Soft Glass page includes a static locally drawn scene and a Miuix sample panel. The sample sits immediately below the master switch; all blur/background/opacity/reflection sliders follow underneath. Slider drafts update the illustration during dragging, while local preferences save on release. The master switch returns the sample to a neutral panel when off. There is no continuous wallpaper animation or extra network/image dependency.

This preview is deliberately a **UI illustration**, not the HyperOS material renderer. Parameters are mapped for visual feedback only and do not promise pixel parity with notifications/Control Center. Exact material rendering and authenticated system configuration remain deferred. Nothing is applied to the framework by this preview.

## HyperImmersive and Island UI

HyperImmersive has its own page with version/status placeholders, StandBy and blurred-background switches, numeric idle/off fields, seconds/minutes menus anchored to the actual unit button with the same undimmed blur as other choices and a zero-time warning. Times are stored in seconds in the existing local preview keys; invalid inputs are not committed. Preview StandBy remains explicitly unavailable until runtime integration.

Lockscreen SuperIsland uses the original HMC-Enhanced IslandDemo WebP assets, unchanged: choosing ColorOS or HyperOS switches the correct animation. The upstream ImageDecoder/ImageView approach is retained, with decoding off the UI thread and lifecycle-aware start/stop. Assets and source attribution are recorded in NOTICE.md; this is UI demo reuse, not a feature-runtime port.

## Boundary

This milestone is **UI only**. Changes persist in Manager preferences and do not apply to the installed framework. There is no root request, native hook engine, permanent service or privileged configuration transport. The existing framework, Settings entry and HMC optimizations are untouched. Removing the splash does not itself implement Settings-to-Manager integration. Signed Manager access/config migration and opening the Manager from HyperOS Settings are future integration work, not implemented here. The UI never reports a fabricated framework-active status. Secret API entry is intentionally deferred until secure connected configuration exists.

## Build

Use JDK21 and AndroidSDK37/build-tools37.0.0. Configure SDK with Android Studio or ignored local.properties, then run:

```
gradlew.bat :app:assembleRelease :app:lintRelease
python scripts/generate_catalog.py
python scripts/verify_catalog.py
```

Output: `app/build/outputs/apk/release/app-release.apk`. This is a debug-signed preview, not a production signing configuration. Never commit signing keys or local.properties.

`catalog.json` was exported from Mixi Framework1.0.4 EnhancedCatalog (92controls) using the original Java initializer, preserving keys/defaults/options. Run the generator after modifying the source catalog; the verifier checks generated metadata stays in sync. The repository verifier checks the shipped data and UI-only app-manifest boundary. `VERIFICATION.json` records build, lint, signature and payload checks. Visual approval on a physical phone is still pending; build/lint do not establish pixel-perfect HyperOS parity.

## Credits

[Miuix](https://github.com/compose-miuix-ui/miuix) — YuKongA and contributors, Apache-2.0. HMC/Enhanced — zyl6932/Lewewe, AGPL-3.0. HyperModifier — AritxOnly. See LICENSE/NOTICE and bundled attribution files. No original feature APKs are bundled.
