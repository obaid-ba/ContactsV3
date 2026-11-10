import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---------------------------------------------------------------------------------------------
// Clé Google Maps : lue dans local.properties (ignoré par Git) ou dans la variable d'environnement
// MAPS_API_KEY. Elle n'apparaît dans aucun fichier du dépôt ; elle est injectée dans le manifeste
// au moment de la compilation via le placeholder ${MAPS_API_KEY}.
// ---------------------------------------------------------------------------------------------
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY")
    ?: System.getenv("MAPS_API_KEY")
    ?: ""

android {
    namespace = "com.example.contactsv3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.contactsv3"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "3.0"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Fragments, ViewModel partagé, RecyclerView
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Orientation EXIF des photos (caméra / galerie)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Google Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    testImplementation("junit:junit:4.13.2")
}
