import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "2.6.3"
val kotlinVersion = "1.6.10"

plugins {
    id("org.springframework.boot") version "2.6.3"
    kotlin("jvm") version "1.6.10"
    java
    application
}

group = "dev.ishikawa.demo.dd_flink.datasource"
version = "0.0.1-SNAPSHOT"


val javaVersionInt = 11
val javaVersion = JavaVersion.toVersion(javaVersionInt)
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = javaVersion.toString()
    }
}


repositories {
    mavenCentral()
    // for kafka-protobuf-serializer
    maven {
        url = uri("http://packages.confluent.io/maven/")
        isAllowInsecureProtocol = true
    }
    // for demo:0.1.0
    mavenLocal()
}


val kafkaClientVersion = "3.1.0"
val kotlinxVersion = "1.6.0"
val kafkaProtobufSerializerVersion = "7.0.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")

    // for kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaClientVersion")

    // for json serializer/deserializer
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")

    // for protobuf serializer/deserializer
    implementation("io.confluent:kafka-protobuf-serializer:$kafkaProtobufSerializerVersion")
    implementation("dev.ishikawa.demo.dd_flink.proto:demo:0.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//tasks.withType<Wrapper> {
//    gradleVersion = "6.9.1"
//}