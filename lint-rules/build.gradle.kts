plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    // Custom lint rules must target JVM 17 or higher for AGP 8.1+
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly("com.android.tools.lint:lint-api:31.2.1")
    compileOnly("com.android.tools.lint:lint-checks:31.2.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.android.tools.lint:lint-tests:31.2.1")
    testImplementation("com.android.tools.lint:lint:31.2.1")
}

// Register your IssueRegistry class so Lint can find your detector
tasks.withType<Jar> {
    manifest {
        attributes("Lint-Registry-V2" to "com.example.lint.MyIssueRegistry")
    }
}
