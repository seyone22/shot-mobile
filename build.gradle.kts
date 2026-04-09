// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Add the KSP plugin required for Room Database compilation
    id("com.google.devtools.ksp") version "2.3.2" apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false

    id("com.google.android.gms.oss-licenses-plugin") version "0.11.0" apply false
}