#!/bin/bash

# Define the output file
OUTPUT_FILE="project_summary.txt"

# Clear the output file if it already exists
> "$OUTPUT_FILE"

# Define directories to scan (adjust paths if your smali folders look different)
TARGET_DIRS=("app/src/main/java" "app/src/main/smali" "smali" "smali_classes2")

echo "Starting concatenation..."

# Loop through predefined target directories
for DIR in "${TARGET_DIRS[@]}"; do
    if [ -d "$DIR" ]; then
        echo "Processing directory: $DIR"
        
        # Find all source files (.java, .kt, .smali, or .xml)
        find "$DIR" -type f \( -name "*.java" -o -name "*.kt" -o -name "*.smali" -o -name "*.xml" \) | while read -r file; do
            
            # Print delimiter and relative file path into the output file
            echo "========================================" >> "$OUTPUT_FILE"
            echo "FILE: $file" >> "$OUTPUT_FILE"
            echo "========================================" >> "$OUTPUT_FILE"
            
            # Append the actual file contents
            cat "$file" >> "$OUTPUT_FILE"
            
            # Ensure there's a trailing newline
            echo -e "\n" >> "$OUTPUT_FILE"
        done
    fi
done

# Also include the AndroidManifest.xml if found in main
if [ -f "app/src/main/AndroidManifest.xml" ]; then
    echo "========================================" >> "$OUTPUT_FILE"
    echo "FILE: app/src/main/AndroidManifest.xml" >> "$OUTPUT_FILE"
    echo "========================================" >> "$OUTPUT_FILE"
    cat "app/src/main/AndroidManifest.xml" >> "$OUTPUT_FILE"
    echo -e "\n" >> "$OUTPUT_FILE"
fi

echo "Done! All files concatenated into $OUTPUT_FILE"
