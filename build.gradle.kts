// build.gradle (Project level)
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false // Use 2.0.21 here
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}