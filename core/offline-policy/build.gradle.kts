plugins {
 id("org.jetbrains.kotlin.multiplatform")
 id("com.android.library")
}
kotlin {
 androidTarget()
 jvm()
 iosArm64()
 iosSimulatorArm64()
 sourceSets {
  commonMain { dependencies { implementation(project(":core:security-state")) } }
  commonTest { dependencies { implementation(kotlin("test")) } }
  jvmTest { dependencies { implementation("junit:junit:4.13.2") } }
 }
}
android {
 namespace = "org.akefadaka.aegis.offline"
 compileSdk = 36
 defaultConfig { minSdk = 26 }
}
tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
 includeConfigs = listOf("jvmCompileClasspath", "jvmRuntimeClasspath", "androidDebugCompileClasspath", "androidDebugRuntimeClasspath")
 failOnUnresolvedDependencies = true
}
