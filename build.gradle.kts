import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    `java-library`
    id("net.researchgate.release") version "2.8.1"
    `maven-publish`
    id("com.adarshr.test-logger") version "3.1.0"
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.0"
    id("com.github.ben-manes.versions") version "0.39.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
    id("org.owasp.dependencycheck") version "6.5.0.1"
}

group = "no.nav.eessi.pensjon"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val oidcTokenSupportVersion = "1.1.6"
val springVersion by extra("5.3.+")
val junitVersion by extra("5.8.+")


dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("no.nav.security:token-validation-spring:$oidcTokenSupportVersion")
    implementation("ch.qos.logback:logback-classic:1.2.7")
    implementation("org.springframework:spring-web:$springVersion")
    implementation("org.springframework:spring-context:$springVersion")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    testImplementation("javax.servlet:javax.servlet-api:4.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:4.1.0")
    testImplementation("org.springframework:spring-test:${springVersion}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

// https://github.com/researchgate/gradle-release
release {
    newVersionCommitMessage = "[Release Plugin] - next version commit: "
    tagTemplate = "release-\${version}"
}

// https://help.github.com/en/actions/language-and-framework-guides/publishing-java-packages-with-gradle#publishing-packages-to-github-packages
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/navikt/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

/* https://github.com/ben-manes/gradle-versions-plugin */
tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        listOf("alpha", "beta", "rc", "cr", "m", "preview", "pr2")
                .any { qualifier -> """(?i).*[.-]${qualifier}[.\d-]*""".toRegex().matches(candidate.version) }
    }
    revision = "release"
}
