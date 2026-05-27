# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.stock.analyzer.data.remote.dto.** { *; }
-keep class com.stock.analyzer.data.model.** { *; }

# Keep Room entities
-keep class com.stock.analyzer.data.local.entity.** { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.stock.analyzer.data.remote.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
