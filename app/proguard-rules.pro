-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep class com.openscan.app.data.db.** { *; }

# OpenCV uses JNI to bind native methods by signature; keep everything so
# R8 doesn't strip or rename members the native side calls into.
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**
