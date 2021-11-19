plugins {
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("de.chojo.publishdata") version "1.0.2"
    java
    `maven-publish`
}

group = "de.eldoria"
version = "1.0"
val shadebase = "de.eldoria." + rootProject.name + ".libs."

repositories {
    mavenCentral()
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-proxies/")
}

dependencies {
    compileOnly("de.eldoria", "schematicbrushreborn-api", "2.0.0-DEV")
    compileOnly("de.eldoria", "eldo-util", "1.12.8-DEV")
    compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.2.6")
    compileOnly("com.plotsquared", "PlotSquared-Core", "6.1.2") // PlotSquared Core API
    compileOnly("com.plotsquared:PlotSquared-Bukkit:6.1.2") { isTransitive = false } // PlotSquared Bukkit API

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.0")
    testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    testImplementation("de.eldoria", "eldo-util", "1.10.11-DEV")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine")
}

license {
    header(rootProject.file("HEADER.txt"))
    include("**/*.java")
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_16
}

publishData {
    hashLength = 7
    useEldoNexusRepos()
    publishTask("jar")
    publishTask("shadowJar")
    publishTask("sourcesJar")
    publishTask("javadocJar")
}

publishing {
    publications.create<MavenPublication>("maven") {
        publishData.configurePublication(this)
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }

            setUrl(publishData.getRepository())
            name = "EldoNexus"
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava{
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    shadowJar{
        relocate("de.eldoria.eldoutilities",  "de.eldoria.schematicbrush.libs.eldoutilities")
        mergeServiceFiles()
    }
    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("plugin.yml") {
                expand(
                    "version" to publishData.getVersion(true)
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
        register<Copy>("copyToServer") {
        val path = project.property("targetDir") ?: "";
        if (path.toString().isEmpty()) {
            println("targetDir is not set in gradle properties")
            return@register
        }
        from(shadowJar)
        destinationDir = File(path.toString())
    }
}
