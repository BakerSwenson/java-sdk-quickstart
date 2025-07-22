plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kafka.tests)
    testImplementation(libs.responsive.tests)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.guava)
    implementation(libs.kafka)
    implementation(libs.responsive)
    implementation(libs.spring.boot)
    implementation(libs.spring.kafka)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.responsive.quickstart.WordCountSpringBoot"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
