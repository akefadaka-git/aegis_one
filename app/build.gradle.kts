plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace="org.akefadaka.aegis"; compileSdk=36
 defaultConfig { applicationId="org.akefadaka.aegis"; minSdk=26; targetSdk=35; versionCode=1; versionName="0.1.0" }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
}
dependencies {
 implementation(project(":core:security-state")); implementation(project(":core:policy"))
 implementation(project(":core:offline-policy")); implementation(project(":core:audit"))
 implementation(project(":platform:android-capabilities"))
 testImplementation("junit:junit:4.13.2")
}
