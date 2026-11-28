plugins { id("org.jetbrains.kotlin.jvm") }
kotlin { jvmToolchain(17) }
dependencies { implementation(project(":core:security-state")); testImplementation("junit:junit:4.13.2") }
tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
 includeConfigs = listOf("compileClasspath", "runtimeClasspath")
 failOnUnresolvedDependencies = true
}
