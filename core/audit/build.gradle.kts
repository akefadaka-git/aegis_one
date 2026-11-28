plugins {
 id("org.jetbrains.kotlin.multiplatform")
 id("com.android.library")
}
kotlin {
 androidTarget()
 jvm()
 iosArm64 {
  compilations.getByName("main") {
   cinterops.create("CommonCrypto") {
    definitionFile.set(project.file("src/nativeInterop/cinterop/CommonCrypto.def"))
   }
  }
 }
 iosSimulatorArm64 {
  compilations.getByName("main") {
   cinterops.create("CommonCrypto") {
    definitionFile.set(project.file("src/nativeInterop/cinterop/CommonCrypto.def"))
   }
  }
 }
 sourceSets {
  commonTest { dependencies { implementation(kotlin("test")) } }
  jvmTest { dependencies { implementation("junit:junit:4.13.2") } }
 }
}
android {
 namespace = "org.akefadaka.aegis.audit"
 compileSdk = 36
 defaultConfig { minSdk = 26 }
}
tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
 includeConfigs = listOf("jvmCompileClasspath", "jvmRuntimeClasspath", "androidDebugCompileClasspath", "androidDebugRuntimeClasspath")
 failOnUnresolvedDependencies = true
}
