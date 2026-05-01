#!/bin/bash
cd "$(dirname "$0")/plugin"
./gradlew build
cp build/output/*.jar ../data/plugins/