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
  commonMain { kotlin.srcDir("src/main/kotlin") }
  commonTest { dependencies { implementation(kotlin("test")) } }
  jvmMain { kotlin.srcDir("src/jvmMain/kotlin") }
  iosMain { kotlin.srcDir("src/iosMain/kotlin") }
  jvmTest {
   kotlin.srcDir("src/test/kotlin")
   dependencies { implementation("junit:junit:4.13.2") }
  }
 }
}
android {
 namespace = "org.akefadaka.aegis.audit"
 compileSdk = 36
 defaultConfig { minSdk = 26 }
}
tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
 failOnUnresolvedDependencies = true
}
