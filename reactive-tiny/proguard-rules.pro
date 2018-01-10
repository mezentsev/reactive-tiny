# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keeppackagenames

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-keepparameternames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keep public interface me.irbis.reactive.Subscriber { *; }
-keep public interface me.irbis.reactive.Subscription { *; }
-keep public interface me.irbis.reactive.Action { *; }
-keep public interface me.irbis.reactive.ObservableOnSubscribe { *; }

-keep public class me.irbis.reactive.CompositeSubscription { public *; }
-keep public class me.irbis.reactive.SimpleSubscription { public *; }
-keep public class me.irbis.reactive.Observable { public *; }
-keep public class me.irbis.reactive.InterruptedExecutor { public *; }

-keep public class me.irbis.reactive.Observable$* { public *; }