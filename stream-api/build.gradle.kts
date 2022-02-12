plugins {
    java
    application
}

group = "dev.ishikawa.demo.dd_flink.wordcount"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


application {
    mainClass.set("dev.ishikawa.demo.dd_flink.WordCountJob")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")


    val flinkVersion = "1.14.3"
    implementation("org.apache.flink:flink-streaming-java_2.11:$flinkVersion")

//    implementation("org.apache.flink:flink-connector-kafka_2.12:$flinkVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}