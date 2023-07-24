import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}

allprojects {
    group = "io.github.rk012"
    version = "0.1"
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    val testImplementation by configurations

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}