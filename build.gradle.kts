plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"

    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    id("com.github.ben-manes.versions") version "0.51.0"

    id("jacoco")

    val kotlinVersion = "1.9.24"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

group = "ru.nemodev"
version = System.getenv("SERVICE_VERSION") ?: "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri(System.getenv("NEXUS_URL") ?: "https://nexus.bistrodengi.ru/repository/maven-releases/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    val nemodevPlatformGroup = "ru.nemodev.platform"
    val nemodevPlatformVersion = "1.0.0"
    implementation("$nemodevPlatformGroup:core-starter:$nemodevPlatformVersion")
    implementation("$nemodevPlatformGroup:core-db:$nemodevPlatformVersion")
    implementation("$nemodevPlatformGroup:core-security-oauth2-resource:$nemodevPlatformVersion")
    implementation("$nemodevPlatformGroup:core-integration-s3-minio:$nemodevPlatformVersion")
    implementation("$nemodevPlatformGroup:core-integration-http:$nemodevPlatformVersion")

    // spring
    val springBootVersion = "3.3.2"
    implementation("org.springframework.boot:spring-boot-starter-mail:$springBootVersion")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose:$springBootVersion")
    kapt("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    val apachePoiVersion = "5.3.0"
    implementation("org.apache.poi:poi:$apachePoiVersion")
    implementation("org.apache.poi:poi-ooxml:$apachePoiVersion")

    // google
    implementation("com.google.apis:google-api-services-sheets:v4-rev612-1.25.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0")
}

jacoco {
    toolVersion = "0.8.12"
}

val excludeTestDirs = listOf(
    "**/Application*",
    "**/api/v1/controller/*",
    "**/api/v1/processor/*",
    "**/config/*",
    "**/service/*",
    "**/repository/*",
    "**/entity/*",
    "**/integration/*"
)

tasks.jacocoTestReport  {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
    }
    // Исключение пакетов/классов из подсчета покрытия
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it)
                .exclude(excludeTestDirs)
        })
    )
}

// Jacoco docs https://reflectoring.io/jacoco/
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    // Исключение пакетов/классов из подсчета покрытия
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it)
                .exclude(excludeTestDirs)
        })
    )
    violationRules {
        rule {
            isEnabled = false   // глобальное правило покрытия всего проекта тестами не менее чем 10%
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.10".toBigDecimal()
            }
        }
        rule {
            isEnabled = false   // правило покрытия каждого пакета тестами не менее чем 10%
            element = "PACKAGE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.10".toBigDecimal()
            }
        }
        rule {
            isEnabled = false   // правило покрытия каждого класса тестами не менее чем 10%
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.10".toBigDecimal()
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.check {
    finalizedBy(tasks.jacocoTestCoverageVerification)
    finalizedBy(tasks.dependencyUpdates)
}

tasks.jar {
    enabled = false
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = "21"
    }
}

springBoot {
    buildInfo()
}
