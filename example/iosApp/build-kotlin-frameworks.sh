#!/bin/bash

# This script builds the Kotlin framework for iOS
cd "$SRCROOT/../.."

# Create the frameworks directory if it doesn't exist
FRAMEWORKS_DIR="$SRCROOT/Frameworks"
mkdir -p "$FRAMEWORKS_DIR"

# Define the debug or release configuration
if [ "$CONFIGURATION" == "Debug" ]; then
    KOTLIN_BUILD_TYPE="Debug"
else
    KOTLIN_BUILD_TYPE="Release"
fi

# Define the build task based on the architecture
if [ "$PLATFORM_NAME" == "iphonesimulator" ]; then
    SIMULATOR=true
    KOTLIN_TARGET="IosX64"
    DESTINATION_FRAMEWORK="$FRAMEWORKS_DIR/simulator"
else
    SIMULATOR=false
    KOTLIN_TARGET="IosArm64"
    DESTINATION_FRAMEWORK="$FRAMEWORKS_DIR/device"
fi

mkdir -p "$DESTINATION_FRAMEWORK"

# Clean previous builds
rm -rf "$DESTINATION_FRAMEWORK/ComposeApp.framework"
rm -rf "$DESTINATION_FRAMEWORK/speechToTextKitCompose.framework"

# Build the frameworks - Adjust paths as needed for your project structure
echo "Building Kotlin frameworks for $KOTLIN_TARGET..."

# Build ComposeApp framework
echo "Building ComposeApp framework..."
./gradlew :example:composeApp:link${KOTLIN_BUILD_TYPE}Framework$KOTLIN_TARGET

# Build speechToTextCompose framework
echo "Building speechToTextCompose framework..."
./gradlew :speechToTextCompose:link${KOTLIN_BUILD_TYPE}Framework$KOTLIN_TARGET

# Copy the frameworks to the destination directory
echo "Copying frameworks to $DESTINATION_FRAMEWORK..."
cp -R "example/composeApp/build/bin/$KOTLIN_TARGET/${KOTLIN_BUILD_TYPE}Framework/ComposeApp.framework" "$DESTINATION_FRAMEWORK"
cp -R "speechToTextCompose/build/bin/$KOTLIN_TARGET/${KOTLIN_BUILD_TYPE}Framework/speechToTextKitCompose.framework" "$DESTINATION_FRAMEWORK"

echo "Kotlin frameworks built successfully"
