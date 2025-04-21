import com.sun.tools.attach.spi.AttachProvider.providers

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.tsl_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tsl_app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "V.1.0.7"
        setProperty("archivesBaseName", "TSL-$versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    buildFeatures {
        viewBinding = true
    }
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.5.0")
    implementation ("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation("androidx.activity:activity:1.9.3")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    //SIZE UNIT
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation (project(":RFIDAPI3Library"))

    // Easy permission
    implementation("pub.devrel:easypermissions:3.0.0")
    //swipe refresh
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}