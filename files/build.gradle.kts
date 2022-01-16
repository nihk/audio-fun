plugins {
    `android-library`
    kotlin("android")
    kotlin("kapt")
    `kotlin-parcelize`
    hilt
}

androidLibraryConfig()

dependencies {
    implementation(project(Modules.core))

    implementation(Dependencies.multidex)
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.vectorDrawable)
    implementation(Dependencies.Dagger.runtime)
    implementation(Dependencies.Dagger.Hilt.runtime)
    implementation(Dependencies.Kotlin.coroutines)

    testImplementation(Dependencies.junit)

    kapt(Dependencies.Dagger.compiler)
    kapt(Dependencies.Dagger.Hilt.compiler)
}
