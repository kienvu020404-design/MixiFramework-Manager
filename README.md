# Mixi Framework Manager

Native Android Manager UI built with Kotlin, Jetpack Compose and Miuix by YuKongA (Apache-2.0), pinned to 0.9.4. Android15+ / targetAndroid17. Separate project from the native framework.

Initial milestone is UI only: local choices do not control the installed framework. No root, native hooks, background service or framework configuration permissions are requested. Settings integration and authenticated configuration transport are separate future work.

Build: JDK21, AndroidSDK37, `gradlew.bat :app:assembleDebug`. SDK path belongs in ignored local.properties. Never commit signing keys or device data.
