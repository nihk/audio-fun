plugins {
    `android-library`
    kotlin("android")
    kotlin("kapt")
    hilt
}

androidLibraryConfig()

dependencies {
    implementation(Dependencies.multidex)
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.Lifecycle.viewModel)
    implementation(Dependencies.inject)
    implementation(Dependencies.Fragment.runtime)
    implementation(Dependencies.Dagger.Hilt.runtime)

    testImplementation(Dependencies.junit)
    defaultAndroidTestDependencies()

    kapt(Dependencies.Dagger.compiler)
    kapt(Dependencies.Dagger.Hilt.compiler)
}
