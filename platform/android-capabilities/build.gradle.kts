plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android {
 namespace="org.akefadaka.aegis.platform"
 compileSdk=36
 defaultConfig { minSdk=26 }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
}
dependencies { implementation(project(":core:security-state")) }
tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
 // Exact Android variant configurations; this library is consumed by the debug APK.
 includeConfigs = listOf("debugCompileClasspath", "debugRuntimeClasspath")
 failOnUnresolvedDependencies = true
}
