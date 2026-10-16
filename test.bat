@echo off
cd /d %~dp0/plugin
call gradlew test -x check