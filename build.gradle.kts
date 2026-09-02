import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    id("net.fabricmc.fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup

repositories {
    maven {
        name = "Modrinth"
        url = URI("https://api.modrinth.com/maven")
    }
    maven {
        name = "CottonMC"
        url = URI("https://server.bbkr.space/artifactory/libs-release")
    }
    // Local libs folder for LibGui
    flatDir {
        dirs("libs")
    }
}

dependencies {
    val minecraftVersion: String by project
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // No more `mappings(...)` — Minecraft ships unobfuscated from 26.1 onward.
    val loaderVersion: String by project
    implementation("net.fabricmc:fabric-loader:$loaderVersion")
    val fabricVersion: String by project
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    val fabricKotlinVersion: String by project
    implementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    // LibGui - using local JAR file due to TLS issues with Cotton Maven on Gradle 9.x
    val libGuiVersion: String by project
    implementation(group = "", name = "LibGui", version = libGuiVersion)
    include(group = "", name = "LibGui", version = libGuiVersion)
}

tasks {
    val javaVersion = JavaVersion.VERSION_25
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}