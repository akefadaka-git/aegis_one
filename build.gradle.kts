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
  failOnUnresolvedDependencies = true
  // These implicit Gradle parent projects contain no shipped code or dependencies.
  if (project.path == ":core" || project.path == ":platform") enabled = false
 }
}
