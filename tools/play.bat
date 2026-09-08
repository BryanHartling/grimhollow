@echo off
setlocal
cd /d "%~dp0.."
for /f "delims=" %%J in ('dir /b /a-d /o-d "desktop\build\libs\desktop-*.jar" 2^>nul') do (
  start "Grimhollow" ".toolchain\jdk-17\bin\javaw.exe" -jar "desktop\build\libs\%%J"
  exit /b 0
)
echo No desktop build found. Run tools\rebuild.bat first.
pause
exit /b 1
