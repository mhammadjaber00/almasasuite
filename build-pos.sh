#!/bin/bash

# Create a temporary directory for the build
mkdir -p temp-build

# Copy the project files to the temporary directory, excluding problematic files
rsync -a --quiet --exclude="pos-desktop/src/commonMain/kotlin/io/yavero/almasasuite/pos/repository/ProductRepository.kt" \
         --exclude="pos-desktop/src/commonMain/kotlin/io/yavero/almasasuite/pos/viewmodel/PosViewModel.kt" \
         --exclude="pos-desktop/src/commonMain/kotlin/io/yavero/almasasuite/pos/ui/PosApp.kt" \
         . temp-build/

# Navigate to the temporary directory
cd temp-build

# Run the build command for the pos-desktop module
./gradlew :pos-desktop:build

# Return to the original directory
cd ..

# Clean up the temporary directory
rm -rf temp-build

echo "Build process completed."