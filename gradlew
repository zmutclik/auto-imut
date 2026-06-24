#!/bin/bash
# Gradle wrapper script
# Download gradle-wrapper.jar if not present

GRADLE_DIR="$(cd "$(dirname "$0")/gradle/wrapper" && pwd)"
GRADLE_JAR="$GRADLE_DIR/gradle-wrapper.jar"
GRADLE_URL="https://services.gradle.org/distributions/gradle-8.11.1-bin.zip"

if [ ! -f "$GRADLE_JAR" ]; then
    echo "Downloading Gradle wrapper..."
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar" -o "$GRADLE_JAR"
fi

exec java -jar "$GRADLE_JAR" "$@"
