import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.BuildType
import java.io.File
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.androidAppConfig(extras: (BaseAppModuleExtension.() -> Unit) = {}) = androidConfig<BaseAppModuleExtension>().run {
    defaultConfig {
        buildToolsVersion = BuildVersion.buildTools
        multiDexEnabled = true
    }

    buildTypes {
        listOf(getByName(BuildTypes.DebugMinified), getByName(BuildTypes.Release)).forEach { buildType ->
            buildType.isShrinkResources = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    extras()
}

fun Project.androidLibraryConfig(extras: (LibraryExtension.() -> Unit) = {}) = androidConfig<LibraryExtension>().run {
    buildFeatures {
        buildConfig = false
    }

    extras()
}

private fun <T : BaseExtension> Project.androidConfig() = android<T>().apply {
    repositories.addProjectDefaults()

    compileSdkVersion(BuildVersion.compileSdk)

    defaultConfig {
        minSdk = BuildVersion.minSdk
        targetSdk = BuildVersion.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName(BuildTypes.Debug) {
            isMinifyEnabled = false
        }
        create(BuildTypes.DebugMinified) {
            signingConfig  = signingConfigs.getByName(BuildTypes.Debug)
            minify(defaultProguardFile())
        }
        getByName(BuildTypes.Release) {
            minify(defaultProguardFile())
        }
    }

    buildFeatures.apply {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }

    testOptions {
        animationsDisabled = true
    }

    packagingOptions {
        setExcludes(
            setOf(
                "LICENSE.txt",
                "NOTICE.txt",
                "META-INF/**",
            )
        )
    }
}.also {
    defaultDependencies()
}

private fun <T : BaseExtension> Project.android(): T {
    @Suppress("UNCHECKED_CAST")
    return extensions.findByName("android") as T
}

fun Project.jvmConfig() {
    defaultDependencies()
}

private fun Project.defaultDependencies() {
    dependencies {
        "implementation"(Dependencies.Kotlin.stdlib)
    }
}

private fun BaseExtension.defaultProguardFile(): File {
    return getDefaultProguardFile("proguard-android.txt")
}

private fun BuildType.minify(defaultProguardFile: File) {
    isMinifyEnabled = true
    proguardFiles(defaultProguardFile, "proguard-rules.pro")
    consumerProguardFiles("consumer-rules.pro")
}

fun BaseExtension.addSharedTestDirectory(name: String) {
    sourceSets {
        listOf("test", "androidTest").forEach { sourceSet ->
            getByName(sourceSet).apply {
                resources.srcDir("src/$name/resources")
                java.srcDir("src/$name/kotlin")
            }
        }
    }
}

fun Project.withKtlint() {
    apply(plugin = Plugins.ktlintGradle)
}
