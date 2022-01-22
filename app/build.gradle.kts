plugins {
    `android-application`
    kotlin("android")
    kotlin("kapt")
    hilt
}

androidAppConfig {
    defaultConfig {
        applicationId = "nick.audio_fun"
        versionCode = 1
        versionName = "1.0"
    }
}

withKtlint()

dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.recordings))
    implementation(project(Modules.recorder))
    implementation(project(Modules.playback))

    implementation(Dependencies.multidex)
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.appCompat)
    implementation(Dependencies.activity)
    implementation(Dependencies.Fragment.runtime)
    implementation(Dependencies.vectorDrawable)
    implementation(Dependencies.constraintLayout)
    implementation(Dependencies.material)
    implementation(Dependencies.Dagger.runtime)
    implementation(Dependencies.Dagger.Hilt.runtime)
    implementation(Dependencies.Lifecycle.runtime)

//    debugImplementation(Dependencies.leakCanary)
    debugImplementation(Dependencies.Fragment.testing)

    testImplementation(Dependencies.junit)
    defaultAndroidTestDependencies()

    kapt(Dependencies.Dagger.compiler)
    kapt(Dependencies.Dagger.Hilt.compiler)
}
