#!/bin/bash
set -e

echo "Copying static versioned assets..."
mkdir -p ${target_dir}
sudo cp -r -f static/assets/* ${target_dir}