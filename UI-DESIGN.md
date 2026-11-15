# UI milestone

Use real Miuix components, automatic light/dark palettes and HyperOS-style large page titles, grouped rounded cards, switches, numeric sliders and separate feature pages. Vietnamese first, accessible touch targets and system text scaling. Home: honest connection state, ROM facts, feature shortcuts. Features: HMC Enhanced category pages, Soft Glass, extensions. About: version, credits, source links, local appearance choices.

Reuse the existing feature catalog keys and Vietnamese labels. Store UI preview choices locally; clearly state that they are not applied to the framework. No simulated active status, root requests or system writes. Keep hooks and HMC optimizations untouched. Navigation remembers its stack, supports system back, and pages preserve scroll. Build the APK and inspect any available runtime preview before calling visuals accepted.


## 0.2 visual revision
Use Miuix FloatingNavigationBar textureBlur and progressiveTextureBlur top gradient, both sampling one content LayerBackdrop. Draw content edge-to-edge, disable navigation contrast scrim, sync system icon brightness to local theme. Keep transitions fixed-size (no size transform); short slide/fade for details and fade for root tabs. Replace marketing hero with a compact neutral unknown-status card; never infer Active/Not Active from a preview toggle. Visual/performance approval on device remains separate from build checks.

## 0.3 navigation/performance revision
Original miuix-nav NavDisplay/MiuixDefault animates complete detail pages including appbar/backdrop, with an actual caller stack. Root floating tabs drive HorizontalPager with one neighbor retained; cancellation always settles on the latest selected tab, without skipping on currentPage equality midway. Stable state/immutable catalog metadata enable Compose skipping. Release preview enables R8/resource shrinking with the existing preview signing identity; no runtime services, hook changes or fake framework writes. Physical frame-time acceptance remains pending.

## 0.4 motion/startup revision
Use original Miuix Folme spring for floating-tab pager: damping .95, response .4seconds, .5px settlement threshold; keep neighbor pages and latest-selection cancellation. Generate immutable catalog metadata during development/build preparation to eliminate startup asset JSON read/parse, retaining the source catalog contract and indexed page lookup. SharedPreferences load remains; no claim that every disk read is removed. Splash window follows system day/night without extra startup Activity. No background preload service, framework writes or hook changes. Physical startup/frame measurements deferred at user request.

## 0.5 unbranded launch
User wants entry from HyperOS Settings to feel like opening a settings page, not a standalone branded app launch. Both day/night themes set windowDisablePreview=true, empty vector splash icon and zero icon animation duration; preserve normal opaque Activity and edge-to-edge behavior. Launcher icon unchanged; no separate splash Activity, delay, keep condition or extra startup service. This controls branding/preview, not cold-start latency or every OEM launch transition; actual Settings integration remains a separate pending task.

## 0.6 Soft Glass sample UI
User explicitly requests layout/preview UI first, exact system appearance later. Add a local static landscape Canvas and rounded sample panel using the existing Miuix blur effect; four local parameters influence blur/tint/highlight illustration. Draft slider values affect only this page during dragging, persisted local values update on release. Clearly label approximate UI/not HyperOS renderer, no framework writes or continuous animation. No image generation/download needed. Phone visual acceptance pending.
