# R8 rules for the release build.
#
# Most of what this app uses ships its own consumer rules inside the library —
# Compose, Hilt, DataStore and Play Billing all do — so this file only carries
# what is specific to us. Adding broad `-keep class **` rules would undo the
# shrinking that makes the release build worth producing.

# Readable stack traces. Without these a crash report from Play is a list of
# single letters, and the mapping file is useless because the line numbers are
# gone as well.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Annotations the DI and Compose tooling reads at build time.
-keepattributes *Annotation*,InnerClasses,Signature,Exceptions

# Widget hosting instantiates provider views by name from another app's
# package. R8 cannot see that edge, but it also cannot rename anything in
# another app — this only keeps our own host subclass intact.
-keep class * extends android.appwidget.AppWidgetHostView { *; }

# The dream is started by the system from the manifest entry, by class name.
# Manifest components are kept automatically; this documents why it matters and
# guards against the class being inlined away if it ever loses its entry.
-keep class com.lsk0522.nightstand.feature.standby.StandbyDreamService { *; }

# Enum constants are stored by name in DataStore and read back with valueOf.
# Renaming them would silently reset every setting on the first release build.
-keepclassmembers enum com.lsk0522.nightstand.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Kotlin coroutines keeps a debug agent reference that is absent at runtime.
-dontwarn kotlinx.coroutines.debug.**
