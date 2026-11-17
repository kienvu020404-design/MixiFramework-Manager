# Six fixes test checkpoint

1. Packaging pins the1.0.6 baseline and verified candidate bridge hashes plus source hashes. It refuses a stale or unverified bridge.
2. Expired queued requests are rejected before mutation. Apply shares the original request budget. Once master is saved, all reload notifications complete. Manager reads state after ambiguous failures.
3. UiStore and its pending writes use a ViewModel scope, preserving Activity recreation. Process-death persistence remains outside this implementation.
4. Preview drafts clear at drag completion so saved/rolled-back store values drive the preview.
5. Unit selection changes presentation only. Decimal minutes roundtrip all supported whole seconds; only edited text is committed.
6. All required gesture methods/fields/monitor policy are resolved before installation. Missing support masks gesture flags. Protection hooks install first. Original installer callbacks must actually be active. Partial gesture installation rolls back its adapter callbacks, disables original flags and retains monitor shutdown/completion protection. Live preferences cannot reenable a failed group in that process. Glass/PIN remain active in ART failure tests.

Tests: release/lint; time values10..86400 roundtrip; blocked-queue deadline; Apply save failure/expiry; original APK ART normal40callbacks/live Glass; missing navigation API, partial draw-hook failure and protection-hook failure; shared API/HMC callback rollback. All passed. Full installed app rotation/preview/game-Home/CPU acceptance still pending. No new version assigned without user request. Rollback Mixi-v1.0.6-Final-Test.zip.
