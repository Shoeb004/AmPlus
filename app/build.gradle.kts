plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.useramplus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.useramplus"
        minSdk = 26
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //rxJava file
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.9")

    //Firebase Ui
    implementation ("com.firebaseui:firebase-ui-auth:8.0.2")

    //Material Library
    implementation ("com.google.android.material:material:1.1.0")

    //ButterKinfe Libray
    implementation ("com.jakewharton:butterknife:10.2.3")
//    annotationProcessor ("com.jakewharton:butterknife-compiler:10.2.3")

    // Dexter
    implementation ("com.karumi:dexter:6.2.2")

    //Location
    implementation("com.google.android.gms:play-services-location:21.2.0")

    //Circle image
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //Firebase storage
    implementation("com.google.firebase:firebase-storage")
    implementation (platform("com.google.firebase:firebase-bom:32.0.0"))

    //Glide
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

//    implementation("com.google.firebase:firebase-iid:+")


    // Fire Cloud message is pending for both the app driver And User

    //GeoFire
    implementation ("com.firebase:geofire-android:3.2.0")

    //RetroFit
    implementation ("com.squareup.retrofit2:adapter-rxjava2:2.6.1")
    implementation ("com.squareup.retrofit2:converter-scalars:2.6.1")
    implementation ("com.squareup.retrofit2:converter-gson:2.6.1")

    //Sliding up
    implementation("com.sothree.slidinguppanel:library:3.3.1")

    //Google places

//    v-20 2min watch
    implementation ("com.google.android.libraries.places:places:3.4.0")

    // ZegoCloud

    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    implementation("com.github.ZEGOCLOUD:zego_inapp_chat_uikit_android:+")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))


    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.github.ZEGOCLOUD:zego_inapp_chat_uikit_android:+")

    //Material Library
    implementation ("com.google.android.material:material:1.11.0")

}