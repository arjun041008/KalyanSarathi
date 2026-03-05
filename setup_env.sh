#!/bin/bash

# Setup script for KalyanSarathi Android project
# This script sets up the necessary environment variables for building the project

# Set JAVA_HOME to JDK 17
export JAVA_HOME=~/jdk17/Contents/Home

# Add JDK 17 to PATH
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java version
echo "Java version:"
java -version

echo ""
echo "Environment setup complete!"
echo "You can now run: ./gradlew build"

