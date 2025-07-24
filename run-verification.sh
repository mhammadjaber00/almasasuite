#!/bin/bash

# Compile the verification application
echo "Compiling verification application..."
kotlinc -cp "shared/build/libs/shared-jvm.jar" verify-models.kt -d verify-models.jar

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful. Running verification application..."
    # Run the verification application
    kotlin -cp "shared/build/libs/shared-jvm.jar:verify-models.jar" VerifyModelsKt
else
    echo "Compilation failed. Check the error messages above."
fi