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

## 0.6.1 preview placement correction
User requires the sample immediately below the Soft Glass master switch. Final order: explanation, master switch, sample, blur controls, material controls. Only layout order changes; preview mappings and local persistence remain unchanged.

## 0.7 undimmed dropdown glass
User reference: anchored dropdown with translucent blurred material and soft rounded corners, no dark veil over the page. All choice controls and theme selector use one shared helper. Reuse original Miuix OverlayListPopup (enableWindowDim=false), ListPopupColumn measurement/scroll, DropdownImpl check/radio semantics, native haptics and animation. The popup host alone clears its default opaque surface; page/root theme stays unchanged. Blur samples the page LayerBackdrop, 28dp clip plus thin highlight border, adaptive popup height. Flat surface fallback when no backdrop exists. Not a SystemUI Glass hook or exact OEM material parity claim. Physical appearance/accessibility interaction still pending on phone.

## 0.7.1 popup corrections
Add10dp gap between trailing value and arrow. Put original Miuix DropdownImpl rows under one full-width scroll Column with propagated minimum row constraints; clip each highlight to22dp corners and6dp side inset, retain28dp popup. Disable only popup texture noiseCoefficient; underlying Miuix blur pipeline remains Gaussian-based. No new shader or global dim added. Phone visual verification pending.

## 0.7.2 consistent menu geometry
The two-choice screenshot exposed the final row touching the container edge and large per-row rounding. Use uniform8dp list inset including top/bottom,2dp inter-row gap,20dp row clipping under28dp outer surface. Set original DropdownImpl isFirst/isLast=false so every row uses the same12dp vertical padding instead of asymmetric first/last20dp. Preserve originalradio/tick/haptics/popupmotion/fullwidth constraints; no per-page patches. Noise remains off and background remains undimmed. Physical verification still required.

## 0.8 extension UI and original Island demo
HyperImmersive matches the provided layout: runtime header (unknown until connected), two switches, inline numeric seconds/minutes timeout controls and zero warning, unavailable preview action clearly identified. Reuse Enhanced StandbyTimeRow UX. Island page reuses original Enhanced IslandDemo + exactColorOS/HyperOS animatedWebP assets, not a customdrawn Island. Decode asynchronously and stop on lifecycle pause/dispose. No runningstatusfabrication or framework writes. Physicalfocus/unit/animation acceptance pending.

## 0.8.1 unit menu anchor/material
Factor one shared GlassChoicePopup from the existing dropdown helper. The HyperImmersive unit button records boundsInWindow; only this anchor is passed to Miuix default position provider, avoiding inference from the whole preference row. Reuse existing blur/tick/roundedgeometry/no-dim style and compact180dp minimum width. Standard choice anchors remain unchanged. Actual phone placement after IME/scroll needs visual approval.
