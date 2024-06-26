import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.5.12"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "me.longi"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    //implementation("com.drewnoakes:metadata-extractor:2.18.0")
    //implementation("androidx.exifinterface:exifinterface:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.commonmark:commonmark:0.21.0")
    //implementation("com.mikepenz:multiplatform-markdown-renderer:0.12.0")
    //implementation("com.mikepenz:multiplatform-markdown-renderer-jvm:0.12.0")

    implementation("io.ktor:ktor-client-core:2.2.4")
    implementation("io.ktor:ktor-client-cio:2.2.4")
    implementation("io.ktor:ktor-client-okhttp:2.2.4")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AnotherUI"
            packageVersion = "1.0.0"
        }
    }
}