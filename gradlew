#!/bin/sh
# Gradle wrapper script - Android Studio will download the actual wrapper jar
# on first sync of this project.

DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME=$(cd "$DIR" >/dev/null 2>&1 && pwd)

# Try to use gradle if installed
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "gradle command not found."
    echo "Please open this project in Android Studio to sync gradle."
    exit 1
fi
