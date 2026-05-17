# ProGuard rules for AiCompanion

# DeepSeek client (openai-kotlin)
-keep class com.aallam.openai.** { *; }

# Sherpa-ONNX native libs
-keep class com.k2fsa.sherpa.onnx.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.aicompanion.data.local.entity.** { *; }
-keep class com.aicompanion.llm.** { *; }
-keep class com.aicompanion.network.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
