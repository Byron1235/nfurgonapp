plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}


android {
    namespace = "com.example.nfurgonapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nfurgonapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.messaging)
    implementation(libs.cronet.embedded)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //RxJava
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.9")

    //FirebaseUI
    implementation("com.firebaseui:firebase-ui-auth:8.0.0")
    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    //Material
    implementation("com.google.android.material:material:1.12.0")

    //Firebase Database
    implementation("com.google.firebase:firebase-database:21.0.0")

    //Dexter
    implementation("com.karumi:dexter:6.1.2")

    //Location
    implementation("com.google.android.gms:play-services-location:17.0.0")

    //GeoFire
    implementation("com.firebase:geofire-android:3.2.0")

    //Circle image
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //Firebase Storage
    implementation("com.google.firebase:firebase-storage:21.0.1")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    //RetroFit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")  // Si usas Gson para JSON
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0") // RxJava 3 adapter
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")


    //EventBus
    implementation("org.greenrobot:eventbus:3.2.0")

    //Circular Progressbar
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")

    //Loading button

    //Loading button 2 usable
    implementation("com.github.razir.progressbutton:progressbutton:2.1.0")

    implementation ("androidx.core:core-ktx:1.12.0")

}