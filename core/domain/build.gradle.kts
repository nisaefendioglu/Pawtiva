import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin business rules (mood, XP, rewards). No Android dependency, fully unit-testable.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":core:model"))
    testImplementation(libs.junit)
}
