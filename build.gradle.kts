plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.scijava.org/content/groups/public") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("org.openimaj:JTransforms:1.3.10")
    implementation("net.imagej:imagej:2.16.0")
    implementation("net.imagej:imagej-legacy:2.0.1")
    implementation("net.imagej:ij:1.54p")
}

tasks.withType<Jar> {
    archiveBaseName.set("ConvolutionPlugin")
    manifest {
        attributes["Main-Class"] = "org.example.ECMADMain"
    }
}
