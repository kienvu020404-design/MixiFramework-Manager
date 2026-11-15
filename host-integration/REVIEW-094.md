# Manager 0.9.4 scoped review

- Removed the project GitHub entry. Keep library attribution/licenses; no repository/source deletion.
- No Manager WAKE_LOCK permission, own service, scheduled job, alarm or periodic polling found. Keep Android cached-process behavior; do not force-stop the shared Host/provider.
- Fixed unnecessary Home/resume status reads: snapshot no longer probes HMC for a field the UI did not use. HyperImmersive query can start its upstream watcher, so query it only from the explicit Immersive page. Refresh on each page RESUMED transition; cancel the page read when paused.
- Preview read now belongs to the page lifecycle, not a detached Activity job. Cancel redundant general refresh jobs/onStop. Keep serialized configuration writes running so leaving the UI does not silently discard submitted changes.
- Stop the original animated Island demo when offscreen/paused and on release; skip identical drawable writes. Existing native Glass visibility/configuration guards and cache integrity checks remain.
- Removed unused runtime-status state and stale imports. Manager adds no debug log loop; retain important existing protocol save/readback errors and authentication checks. Build/test stdout markers are local tooling, not APK logging.
- Extensions Apply is an explicit user action. Same Settings sender authentication; after the existing config drain, flush the two saved battery booleans through the original provider and emit existing MASTER_RELOAD to SystemUI only. Provider failure prevents reload. A queued expired action cannot perform a delayed reload. Button describes the SystemUI restart. Glass stays live and has no Apply button.

Validation: release/lint, 92-field validation, source-extracted concurrent ACK and Apply execution fixtures, lifecycle/read/source-link contracts, unchanged HMC/native payload comparison, signature and ZIP checks PASS. Independent source review addressed Immersive resume freshness. No on-device 0.9.4 installation or visual/overnight battery acceptance yet; previous 0.9.3 measurements are not proof for this build.
