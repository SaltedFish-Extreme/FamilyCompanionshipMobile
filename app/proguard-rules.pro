
-optimizationpasses 5

-dontusemixedcaseclassnames

-dontskipnonpubliclibraryclasses

-dontskipnonpubliclibraryclassmembers

-dontpreverify

-verbose
-printmapping priguardMapping.txt

-optimizations !code/simplification/artithmetic,!field/*,!class/merging/*

################common###############
 #实体类不参与混淆
-keep class com.hisense.sound.logic.**{ *; }
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable
-keepattributes Signature
-keep class **.R$* { *; }
-ignorewarnings
-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclasseswithmembernames class * { # 保持native方法不被混淆
    native <methods>;
}

-keepclassmembers enum * {  # 使用enum类型时需要注意避免以下两个方法混淆，因为enum类的特殊性，以下两个方法会被反射调用，
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

################support###############
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-dontwarn android.support.**

-keep class com.google.android.material.** { *; }
-keep class androidx.** { *; }
-keep public class * extends androidx.**
-keep interface androidx.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
################glide###############
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# 保留自定义控件(继承自View)不能被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get* ();
}

################Gson解析###############
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson

################声音复刻###############
-keep class com.baker.engrave.lib.bean.** { *; }
-keep public class com.baker.engrave.lib.BakerVoiceEngraver{*;}
-keep public class com.baker.engrave.lib.util.HLogger{*;}
-keep public class com.baker.engrave.lib.net.WebSocketClient{*;}
-keep public class com.baker.engrave.lib.callback.DetectCallback{*;}
-keep public class com.baker.engrave.lib.callback.PlayListener{*;}
-keep public class com.baker.engrave.lib.callback.InitListener{*;}
-keep public class com.baker.engrave.lib.callback.MouldCallback{*;}
-keep public class com.baker.engrave.lib.callback.RecordCallback{*;}
-keep public class com.baker.engrave.lib.callback.UploadRecordsCallback{*;}
-keep public class com.baker.engrave.lib.callback.ContentTextCallback{*;}
-keep public class com.baker.engrave.lib.callback.BaseMouldCallback{*;}