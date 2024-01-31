plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    // Default when creating
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // https://projectlombok.org/setup/gradle
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
}

val jarName = "ChatBoardServer"
val jarVersion = "0.0.1"

tasks.jar {

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    archiveBaseName.set(jarName)
    version = jarVersion

    manifest.attributes["Main-Class"] = "server/Core"

    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map { zipTree(it) }
    from(dependencies)
}

tasks.test {
    useJUnitPlatform()
}