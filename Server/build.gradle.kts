plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"

    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "1.9.23"
}

group = "com.nakaharadev"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.hsqldb:hsqldb:2.4.0")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jboss.logging:jboss-logging:3.3.0.Final")
    implementation("org.hibernate:hibernate-entitymanager:3.6.0.Final")
    implementation("org.hibernate.search:hibernate-search-mapper-orm-orm6:6.2.1.Final")
    implementation("org.hibernate.search:hibernate-search-backend-lucene:6.2.1.Final")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}