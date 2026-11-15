# Settings opening optimization stage

UI/versions intentionally held at Manager0.9.4 / Framework1.0.4 until this benchmark stage completes.

Inspected the exact stock Settings APK setIcon method on OS4.0.4.0.XOBCNXM. The owned Mixi row previously executed OEM icon resource assignment/possible bitmap conversion, then Mixi overwrote it with the already-cached scaled icon. On that exact firmware only, handle our unique header in a before-hook and skip that redundant original. Null guards preserve safety. Foreign rows remain unhandled. Unknown firmware retains the exact previous after-hook. Cache holder.icon Field; keep icon scaling and visibility unchanged. No APK Settings, ART filter/cache, HMC/native or background boot changes.

Source-extracted callback tests: owned bitmap/visibility and one write, foreign row original execution, null guards PASS. Independent source review: no actionable findings. Canonical non-SettingsExtension core, HMC/API/resources/native preservation, signature and ZIP checks PASS.

Device baseline via benchmark_settings.py --label before-settingsopt --runs5: actual COLD [216,226,223,231,224], median224ms; actual HOT [35,38,44,34,34,47,32,31,33,33], median34ms. No actual WARM sample. Only background/cached Settings is killed for controlled cold starts; no force-stop, install, app-data or ART changes. Do not infer gains before the same after procedure. Thermal/frequency not controlled.

Candidate Mixi-v1.0.4-SettingsOpt-Test.zip copied to PC/phone Download, hash ae3f2877a92f332142e483e9b94bf605545f3b1e192442f9daba4dfb9aec1ccb verified. User manual install/reboot pending per AGENTS. Versions/UI work remains deferred until this stage is measured. Rollback: previous0.9.4 module; no data deletion.
