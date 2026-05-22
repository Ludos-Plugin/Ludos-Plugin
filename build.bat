@echo off
cd /d %~dp0/plugin
call gradlew build
xcopy /Y /I build\output\*.jar ..\data\plugins\