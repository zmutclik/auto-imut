# AutoClicker ProGuard Rules
-keepattributes *Annotation*
-keep class com.imut.autoclicker.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
