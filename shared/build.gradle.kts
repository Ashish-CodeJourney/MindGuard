plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
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
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
            implementation(libs.koin.core)
            implementation(libs.sqldelight.driver.native)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.turbine)
        }

        androidMain.dependencies {
            implementation(libs.kotlin.stdlib)
            implementation(libs.sqldelight.driver.android)
        }
    }
}

android {
    namespace = "com.mindguard.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("MindGuardDatabase") {
            packageName.set("com.mindguard.shared.data")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
        }
    }
}
