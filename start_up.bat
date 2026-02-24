@echo off

:CHECKMYSQL
sc query MySQL80 | find "RUNNING" > nul
if %errorlevel% neq 0 (
    timeout /t 5 > nul
    goto CHECKMYSQL
)

cd /d "C:\Users\usuario\Downloads"

start "" javaw -jar cocinaruby-1.0-SNAPSHOT.jar

timeout /t 2 > nul
exit /b
