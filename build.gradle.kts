import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
/*
    Plugins
 */
plugins {
    java
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    application
}

group = "org.skillshare"
version = "1.0-SNAPSHOT"

/*
    Repositories
 */
repositories {
    mavenCentral()
    jcenter()
}

/*
    Dependencies
 */
val deps = listOf(
    "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.+",
    "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.+",
    "com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+",
    "io.ktor:ktor-server-netty:1.2.6",
    "io.ktor:ktor-jackson:1.2.6",
    "ch.qos.logback:logback-classic:1.2.1",
    "org.codehaus.groovy:groovy:2.4.15"
)

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    deps.forEach { implementation(it) }
    testImplementation("junit:junit:4.+")
}

/*
    Plugin and Task configuration
 */
application {
    mainClassName = "com.skillshare.tinyurl.server.Server"
}

tasks.withType<ShadowJar> {
    baseName = "skillshare-tinyurl"
    classifier = null
    version = null
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

// show task dependencies
gradle.taskGraph.whenReady {
    allTasks.forEach { task ->
        task.dependsOn.forEach { dependency ->
            println("$task -> $dependency")
        }
    }
}
