# Mixi Framework Manager

Standalone Android UI built with Kotlin, Jetpack Compose and **Miuix by YuKongA**, pinned to 0.9.4. Android15+ / targetAndroid17. Package `com.mixi.manager`, UI milestone `0.2.0-ui`.

## Implemented

- HyperOS-style large titles, grouped cards, Miuix floating blurred navigation and back buttons.
- Progressive TopAppBar blur from the original Miuix effects, content drawn behind bars, transparent gesture navigation with contrast scrim disabled.
- Compact framework status card (unknown/disconnected until integration) and fixed-size slide/fade page transitions without resizing the whole page.
- Home ROM/device/Android/kernel facts, feature shortcuts, honest disconnected state.
- Searchable92-field catalog across10groups: HMC Enhanced subpages, Soft Glass and extensions.
- Original Miuix switches, overlay dropdowns and sliders with live numbers; slider previews persist on release.
- System/light/dark themes and preserved page scroll; Mixi logo, about and source credits.

## Boundary

This milestone is **UI only**. Changes persist in Manager preferences and do not apply to the installed framework. There is no root request, native hook engine, permanent service or privileged configuration transport. The existing framework, Settings entry and HMC optimizations are untouched. Signed Manager access/config migration and opening the Manager from HyperOS Settings are future integration work, not implemented here. The UI never reports a fabricated framework-active status. Secret API entry is intentionally deferred until secure connected configuration exists.

## Build

Use JDK21 and AndroidSDK37/build-tools37.0.0. Configure SDK with Android Studio or ignored local.properties, then run:

```
gradlew.bat :app:assembleDebug :app:lintDebug
python scripts/verify_catalog.py
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. This is a debug-signed preview, not a production signing configuration. Never commit signing keys or local.properties.

`catalog.json` was exported from Mixi Framework1.0.4 EnhancedCatalog (92controls) using the original Java initializer, preserving keys/defaults/options. The repository verifier checks the shipped data and UI-only app-manifest boundary. `VERIFICATION.json` records build, lint, signature and payload checks. Visual approval on a physical phone is still pending; build/lint do not establish pixel-perfect HyperOS parity.

## Credits

[Miuix](https://github.com/compose-miuix-ui/miuix) — YuKongA and contributors, Apache-2.0. HMC/Enhanced — zyl6932/Lewewe, AGPL-3.0. HyperModifier — AritxOnly. See LICENSE/NOTICE and bundled attribution files. No original feature APKs are bundled.
