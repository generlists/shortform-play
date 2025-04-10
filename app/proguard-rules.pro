-keepattributes Signature
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**

# Application classes that will be serialized/deserialized over Gson
-keep class com.sean.ratel.android.data.* { *; }

-keepnames class * implements java.lang.reflect.ParameterizedType
-keepnames class * implements java.lang.annotation.Annotation

-keepclassmembers class * implements java.lang.reflect.InvocationHandler {
    public java.lang.Object invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]);
}

#gson
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
##---------------End: proguard configuration for Gson  ----------

-dontoptimize

# Retrofit,Network 라이브러리 관련 클래스 보존
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }


-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowobfuscation,allowshrinking class retrofit2.Response


# Firebase SDK의 모든 클래스 유지
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
#admob
-keep class com.google.ads.** # Don't proguard AdMob classes
-dontwarn com.google.ads.** # Temporary workaround for v6.2.1. It gives a warning that you can ignore

#라인정보
-keepattributes SourceFile,LineNumberTable
