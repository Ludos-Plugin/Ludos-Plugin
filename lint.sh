#!/bin/bash
cd "$(dirname "$0")/plugin"
./gradlew check -x test