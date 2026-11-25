plugins {
 id("com.android.application") version "8.13.2" apply false
 id("com.android.library") version "8.13.2" apply false
 id("org.jetbrains.kotlin.android") version "2.2.20" apply false
 id("org.jetbrains.kotlin.jvm") version "2.2.20" apply false
 id("org.cyclonedx.bom") version "3.6.1"
}
allprojects {
 group="org.akefadaka.aegis"
 version="0.1.0"
 tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
  // Scope the aggregate SBOM to dependency classpaths that describe shipped code.
  // Android modules are variant-aware; the CI artifact is the debug APK.
  includeConfigs = if (path == ":app" || path == ":platform:android-capabilities") {
   listOf("^debug(Compile|Runtime)Classpath$")
  } else {
   listOf("^(compile|runtime)Classpath$")
  }
  failOnUnresolvedDependencies = true
 }
}
