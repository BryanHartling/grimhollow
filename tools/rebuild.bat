@echo off
setlocal
cd /d "%~dp0.."
set "JAVA_HOME=%CD%\.toolchain\jdk-17"
set "GRADLE_USER_HOME=%CD%\.gradle-user-home"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call gradlew.bat desktop:dist -PdesktopOnly=true --no-daemon
if errorlevel 1 (
  pause
  exit /b 1
)
call tools\play.bat
