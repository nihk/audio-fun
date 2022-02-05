plugins {
    `android-library`
    kotlin("android")
    `kotlin-parcelize`
}

androidLibraryConfig()

dependencies {
    implementation(project(Modules.core))

    implementation(Dependencies.multidex)
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.vectorDrawable)
    implementation(Dependencies.Kotlin.coroutines)

    testImplementation(Dependencies.junit)
}
