plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }
val hostUi = providers.gradleProperty("hostUi").orNull == "true"
android {
 namespace = "com.mixi.manager"
 compileSdk { version = release(37) }
 buildToolsVersion = "37.0.0"
 defaultConfig {
  applicationId = if(hostUi) "com.mixi.host" else "com.mixi.manager"
  minSdk = 35; targetSdk = 37; versionCode = 18; versionName = "0.9.3-host-ui"
  buildConfigField("boolean", "HOST_INTEGRATED", hostUi.toString())
 }
 if(hostUi) androidResources { additionalParameters += listOf("--package-id", "0x80") }
 buildFeatures { compose = true; buildConfig = true }
 buildTypes {
  getByName("release") {
   isMinifyEnabled = true
   isShrinkResources = true
   signingConfig = signingConfigs.getByName("debug")
   proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
  }
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
}
kotlin { jvmToolchain(21); compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) } }
dependencies {
 implementation("androidx.activity:activity-compose:1.13.0")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
 implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.4")
 implementation("top.yukonga.miuix.kmp:miuix-preference-android:0.9.4")
 implementation("top.yukonga.miuix.kmp:miuix-icons-android:0.9.4")
 implementation("top.yukonga.miuix.kmp:miuix-blur-android:0.9.4")
 implementation("top.yukonga.miuix.kmp:miuix-nav-android:0.9.4")
 constraints {
  implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.11.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
 }
}
