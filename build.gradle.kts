plugins {
    id("kotlin-multiplatform") version Versions.kotlin
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm()
        js()

        linuxArm32Hfp()
        linuxMips32()
        linuxMipsel32()
        linuxX64()
        macosX64()
        mingwX64()
        wasm32()

        iosArm32()
        iosArm64()
        iosX64()
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        getByName("jsMain") {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        getByName("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

group = "de.edwardday"
version = "0.2.0-SNAPSHOT-1"
