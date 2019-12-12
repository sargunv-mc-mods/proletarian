import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import com.palantir.gradle.gitversion.VersionDetails
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val minecraftVersion: String by project
val curseProjectId: String by project
val curseMinecraftVersion: String by project
val modJarBaseName: String by project
val modMavenGroup: String by project

plugins {
    java
    kotlin("jvm") version "1.3.60"
    idea
    `maven-publish`
    id("fabric-loom") version "0.2.7-SNAPSHOT"
    id("com.palantir.git-version") version "0.11.0"
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

base {
    archivesBaseName = modJarBaseName
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "http://maven.fabricmc.net")
    maven(url = "http://server.bbkr.space:8081/artifactory/libs-release")
}

val gitVersion: groovy.lang.Closure<Any> by extra
val versionDetails: groovy.lang.Closure<VersionDetails> by extra

version = "${gitVersion()}+mc$minecraftVersion"
group = modMavenGroup

minecraft {
}

configurations {
    listOf(mappings, modCompile).forEach {
        it {
            resolutionStrategy.activateDependencyLocking()
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.2:v2")
    modImplementation("net.fabricmc:fabric-loader:0.7.2+build.174")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.4.24+build.279-1.15")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.3.60+")

    modImplementation("io.github.cottonmc:Jankson-Fabric:2.0.0+j1.2.0")
    include("io.github.cottonmc:Jankson-Fabric:2.0.0+j1.2.0")
}

val processResources = tasks.getByName<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        filter { line -> line.replace("%VERSION%", "${project.version}") }
    }
}

val javaCompile = tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val jar = tasks.getByName<Jar>("jar") {
    from("LICENSE")
}

val remapJar = tasks.getByName<RemapJarTask>("remapJar")

if (versionDetails().isCleanTag) {

    curseforge {
        if (project.hasProperty("curseforge_api_key")) {
            apiKey = project.property("curseforge_api_key")!!
        }

        project(closureOf<CurseProject> {
            id = curseProjectId
            changelog = file("changelog.txt")
            releaseType = "release"
            addGameVersion(curseMinecraftVersion)
            addGameVersion("Fabric")
            relations(closureOf<CurseRelation>{
                requiredDependency("fabric-api")
                requiredDependency("fabric-language-kotlin")
            })
            mainArtifact(file("${project.buildDir}/libs/${base.archivesBaseName}-$version.jar"))
            afterEvaluate {
                uploadTask.dependsOn(remapJar)
            }
        })

        options(closureOf<Options> {
            forgeGradleIntegration = false
        })
    }

}
