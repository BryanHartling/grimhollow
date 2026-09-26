@echo off
setlocal
cd /d "%~dp0.."
for /f "delims=" %%J in ('dir /b /a-d /o-d "desktop\build\libs\desktop-*.jar" 2^>nul') do (
  for /f "delims=" %%E in ('powershell -NoProfile -Command "$v='%%J'.Substring(8).Replace('.jar',''); $p=Join-Path $PWD ('desktop/build/windows/'+$v+'/Grimhollow/Grimhollow.exe'); if(Test-Path -LiteralPath $p){$p}"') do (
    start "Grimhollow" "%%E"
    exit /b 0
  )
  start "Grimhollow" ".toolchain\jdk-17\bin\javaw.exe" -jar "desktop\build\libs\%%J"
  exit /b 0
)
echo No desktop build found. Run tools\rebuild.bat first.
pause
exit /b 1
