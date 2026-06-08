# MindGuard ProGuard rules

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Koin
-keep class org.koin.** { *; }
-keepnames class * extends org.koin.core.module.Module

# DataStore
-keep class androidx.datastore.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# MindGuard shared module — keep all domain models and rules
-keep class com.mindguard.shared.** { *; }

# Accessibility service must not be obfuscated
-keep class com.mindguard.accessibility.** { *; }

# Keep accessibility service metadata
-keepattributes *Annotation*
-keepattributes EnclosingMethod
