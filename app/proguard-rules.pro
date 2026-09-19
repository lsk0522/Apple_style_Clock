# Keep Compose runtime metadata used by the tooling.
-dontwarn org.jetbrains.annotations.**

# Hilt / Dagger generated components are referenced reflectively.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# TODO(next): Phase 11 — tighten these once release build is profiled.
