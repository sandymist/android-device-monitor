# consumer-rules.pro

-keepclassmembers class com.sandymist.android.debuglib.DebugLib {
    public *;
}

-keep class com.sandymist.android.debuglib.ui.DebugScreenKt { *; }

# Keep kotlinx.serialization classes and annotations
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keep @kotlinx.serialization.SerialName class * { *; }

# Keep classes with the @Serializable annotation on fields
-keep class ** {
    @kotlinx.serialization.Serializable <fields>;
}

# Preserve Kotlin metadata for reflection and serialization
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Unit { *; }

# Preserve internals of kotlinx.serialization
-keep class kotlinx.serialization.internal.** { *; }
-keep class kotlinx.serialization.descriptors.** { *; }
