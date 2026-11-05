import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// ------------------------------------------------------------------ API
// URL base configurable sin tocar código versionado:
//   1) local.properties (no se versiona): proyectwin.apiBaseUrl[.debug|.release]
//   2) -Pproyectwin.apiBaseUrl[.debug|.release] al invocar Gradle
// Por defecto 10.0.2.2:8000 apunta al host desde el emulador Android; para un
// dispositivo físico define la IP LAN del backend en local.properties.
val localProperties = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

fun apiBaseUrl(buildType: String): String =
    localProperties.getProperty("proyectwin.apiBaseUrl.$buildType")
        ?: localProperties.getProperty("proyectwin.apiBaseUrl")
        ?: (project.findProperty("proyectwin.apiBaseUrl.$buildType") as String?)
        ?: (project.findProperty("proyectwin.apiBaseUrl") as String?)
        ?: "http://10.0.2.2:8000/v1/"

// ------------------------------------------------------------------ FIRMA
// La keystore y sus contraseñas nunca se versionan. La ruta se puede cambiar
// con PROYECTTWIN_KEYSTORE_PROPERTIES; cada valor admite override por entorno.
val keystoreProperties = Properties().apply {
    val ruta = System.getenv("PROYECTTWIN_KEYSTORE_PROPERTIES")
        ?: "${System.getProperty("user.home")}/.proyecttwin/keystore.properties"
    val archivo = rootProject.file(ruta)
    if (archivo.isFile) archivo.inputStream().use { load(it) }
}

fun signingValue(env: String, prop: String): String? =
    System.getenv(env)?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty(prop)?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingValue("PROYECTTWIN_STORE_FILE", "storeFile")
val releaseStorePassword = signingValue("PROYECTTWIN_STORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingValue("PROYECTTWIN_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingValue("PROYECTTWIN_KEY_PASSWORD", "keyPassword")

android {
    namespace = "com.example.proyectwin"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.proyectwin"
        minSdk = 26
        targetSdk = 36
        versionCode = 10
        versionName = "1.4.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile?.let { file(it) }
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
            enableV1Signing = false
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = false
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl("debug")}\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Distribución: define `proyectwin.apiBaseUrl.release` con el
            // endpoint HTTPS real. El fallback de emulador es HTTP y queda
            // bloqueado por network_security_config en release.
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl("release")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        encoding = "UTF-8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}