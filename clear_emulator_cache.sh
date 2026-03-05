#!/bin/bash

echo "Clearing Android Emulator Cache for KalyanSarathi App..."

# Uninstall the app from emulator
adb uninstall com.example.kalyansarathi

# Clear emulator cache (if possible)
adb shell pm clear com.android.launcher3

# Rebuild and install
echo "Rebuilding and installing app..."
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

echo "Done! The app should now show the correct KalyanSarathi icon."

