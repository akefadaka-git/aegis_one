pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name="AEGIS-ONE"
include(":app",":core:security-state",":core:policy",":core:offline-policy",":core:audit",":platform:android-capabilities")
