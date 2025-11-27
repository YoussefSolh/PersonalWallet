# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ===================================
# Personal Wallet Specific Rules
# ===================================

# Keep Room entities and DAOs
-keep class com.youssefsolh.personalwallet.data.local.entity.** { *; }
-keep class com.youssefsolh.personalwallet.data.local.dao.** { *; }
-keep class com.youssefsolh.personalwallet.domain.model.** { *; }

# Room
-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn com.google.errorprone.annotations.**

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_MembersInjector { *; }
-keep class **_Factory { *; }
-keep class **_Impl { *; }

# Google Sign-In
-keep class com.google.android.gms.** { *; }
-keep class com.google.api.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.api.**

# Google Drive API
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.youssefsolh.personalwallet.**$$serializer { *; }
-keepclassmembers class com.youssefsolh.personalwallet.** {
    *** Companion;
}
-keepclasseswithmembers class com.youssefsolh.personalwallet.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes for serialization
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    <fields>;
    <init>(...);
}

# Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Lifecycle
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep generic signatures for reflection
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*

# Keep exceptions
-keepattributes Exceptions

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===================================
# SQLCipher Database Encryption
# ===================================
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# ===================================
# Optimization Settings
# ===================================
-optimizationpasses 5
-allowaccessmodification
-repackageclasses
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ===================================
# ViewModels
# ===================================
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# ===================================
# Navigation
# ===================================
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# ===================================
# Additional Warnings to Ignore
# ===================================
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn javax.annotation.**
