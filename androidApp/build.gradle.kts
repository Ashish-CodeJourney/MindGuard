plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.kotlin.stdlib)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.compose.tooling)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.accessibility)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(project(":shared"))
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.mindguard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mindguard"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
