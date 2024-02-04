plugins {
    id("java")
    id("org.springframework.boot") version "2.6.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // https://projectlombok.org/setup/gradle
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }

    implementation("org.postgresql:postgresql")  // Postgres JDBC driver
}

val jarName = "ChatBoardServer"

springBoot {
    mainClass.set("app.Entrypoint")
}

/*tasks.jar {

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    archiveBaseName.set(jarName)
    version = jarVersion

    manifest.attributes["Main-Class"] = "database/Entrypoint"

    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map { zipTree(it) }
    from(dependencies)
}*/

tasks.test {
    useJUnitPlatform()
}