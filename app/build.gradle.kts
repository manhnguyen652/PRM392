plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.decider"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.decider"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // RecyclerView for lists
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // ViewPager2 for templates
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // Fragment support
    implementation("androidx.fragment:fragment:1.6.2")
    
    // Preference for settings storage
    implementation("androidx.preference:preference:1.2.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}