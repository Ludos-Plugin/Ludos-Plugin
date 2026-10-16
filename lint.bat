@echo off
cd /d %~dp0/plugin
call gradlew check -x test