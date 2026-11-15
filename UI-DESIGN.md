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

## 0.8.2 compact popup width
User requests less empty horizontal menu space. Measure the shared surface using IntrinsicSize.Max inside a200–240dp default width range instead of expanding the list to288dp. Pass the same minimum to the original OverlayListPopup measurement; unit menus retain180dp minimum. Text wraps if needed, rowheight/accessible radio/tick behavior and8dpedgeinsets remain. Keep blur and no dim. Physical narrow/longlabel approval pending.

## 0.8.3 search to Home title state
Keep a separate remembered Miuix scroll behavior for each root tab and connect each retained pager page to its own behavior. Previously all pages connected to the selected tab behavior, allowing outgoing search content to affect Home. Only the active tab accepts scroll while the pager is settled. Clear focus and hide IME when changing tabs, including Back to Home. Detail navigation, popup and framework runtime are unchanged. Build/lint checks cannot establish visual reproduction; phone search/keyboard/tab/back regression remains pending.

## 0.9.0 Host integration
Same UI is embedded inside MixiHost, launched from the owned Settings header into a separate :manager process. Private module/resource package0x80 and boot-parent classloader preserve original HMC resource IDs and avoid dependency collisions. UI Activity adds only its own ResourcesLoader. Settings retains authenticated configuration authority; catalog validation and existing protocols are reused. No preview preference import, fabricated active state or runtime-auth relaxation. Writes coalesce; text commits on Done/focus loss, secret text is masked with password input, time/hex boundaries retained. Bound foreground replies independently from runtime drains and preserve newer Glass edits when an older write ACK arrives. Build/static tests and independent review passed; user deferred phone execution.

## 0.9.1 connected card and OEM preview path
Connected relay uses green card/check; it does not certify every hook. Routine saved/pending/runtime copy removed. Soft Glass preview requests bounded wallpaper JPEG and original APK d52.r recipe through authenticated Settings relay on page entry, decodes off main, uses OEM setMiGlass/blur APIs on a local View and clears Glass on release. No simulated material fallback. Live wallpaper frames, OEM hidden API permission and actual recipe loading/render parity are not verified. HMC/native payload preserved.

Cache-start optimization: validate trusted cached module first; read/hash embedded module only when cache missing/corrupt. No removed integrity check. No force-stop or process killer: background cached process is allowed with no active work. Baseline launch292ms cold/81ms warm/28ms hot; 30-second CPU sample in BENCHMARK.json. Post-install startup speed is pending; desktop checksum fixture is not Android launch proof.

## 0.9.2 card balance and OEM material init
Remove status card8dp horizontal inset, raise minimum height156 to164dp. Phone screenshot proved actual wallpaper and ordinary blur worked but did not prove native material parity. ROM View DEX identifies small/big Glass radii; original APK b81.b uses36/72 and material1, pass-window blur and blend101. Correct local preview initialization order to these native prerequisites, scale background independently from80px baseline, then original recipe last. Original renderer/recipe reused, no new shader; HMC/runtime/cache path unchanged. Build/local checks pass; physical0.9.2 material acceptance pending.

## 0.9.3 theme, scroll and Settings work
Tick inset16dp and circle stroke inside its Canvas. Connected card selects light green/dark green text or dark green/light text based on the active Miuix surface, including app theme override. Reuse identical wallpaper bitmap; apply native configuration only when its key changes. Native Glass is inactive outside the window viewport or below RESUMED; lifecycle observer is removed on disposal. No polling or reduced visible blur quality. Remove obsolete native-panel reflection search from generated SettingsExtension, keep trusted loading/navigation; skip routine ALREADY_PLACED log. No OEM APK/ART/compiler filter changes. BENCHMARK.json contains before metrics; post093 is pending manual installation.
