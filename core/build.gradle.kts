plugins {
    `android-library`
    kotlin("android")
}

androidLibraryConfig()

dependencies {
    implementation(Dependencies.multidex)
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.Lifecycle.viewModel)
    implementation(Dependencies.Fragment.runtime)

    testImplementation(Dependencies.junit)
    defaultAndroidTestDependencies()
}
