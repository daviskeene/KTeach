#!/bin/bash

mkdir -p classes
# Find our kotlin files
kotlin_files=$(find src/main/kotlin/Grading/ -name "*.kt")
echo $kotlin_files
kotlinc -include-runtime -d test_$1.jar classes $kotlin_files