#!/bin/bash
set -e

echo "Copying static versioned assets..."
mkdir -p ${target_dir}
cp -r -f static/assets/* ${target_dir}