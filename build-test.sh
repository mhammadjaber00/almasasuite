#!/bin/bash

# Set the main class to the test application
export MAIN_CLASS="io.yavero.almasasuite.pos.TestMainKt"

# Build the test application
./gradlew :pos-desktop:build -PmainClass=$MAIN_CLASS

# Run the test application if the build was successful
if [ $? -eq 0 ]; then
    echo "Build successful. Running the test application..."
    ./gradlew :pos-desktop:run -PmainClass=$MAIN_CLASS
else
    echo "Build failed. Check the error messages above."
fi