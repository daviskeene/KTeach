import java.util.regex.Pattern.compile

plugins {
    kotlin("jvm") version "1.3.61"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    distribution
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

compile("ch.qos.logback:logback-classic:1.2.3")

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.google.cloud:google-cloud-firestore:0.60.0-beta")
    val ktorVersion = "1.3.0"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.slf4j:slf4j-simple:1.7.26")
}
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
