plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "dev.ishikawa.demo.dd_flink"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

val javaVersionInt = 11
val javaVersion = JavaVersion.toVersion(javaVersionInt)
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("org.apache.flink:flink-table-common:1.14.3")
    implementation("dev.ishikawa.demo.dd_flink.proto:demo:0.1.0")
    implementation("com.google.protobuf:protobuf-java:3.19.4")
    implementation("com.google.protobuf:protobuf-java-util:3.19.4")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}