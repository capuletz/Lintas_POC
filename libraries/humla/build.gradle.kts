plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "se.lublin.humla"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api("com.google.protobuf:protobuf-java:3.11.4")
    api("com.madgag.spongycastle:core:1.51.0.0")
    api(files("libs/prov-1.51.0.0.jar", "libs/pkix-1.51.0.0.jar"))
    implementation("com.googlecode.javacpp:javacpp:0.7")
    implementation("org.jetbrains:annotations:18.0.0")
    implementation("org.minidns:minidns-hla:0.3.4")
    implementation("org.minidns:minidns-android21:0.3.4")
    implementation("com.google.guava:guava:28.2-android")
    testImplementation(libs.junit)
}
