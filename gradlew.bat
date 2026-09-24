@if "%DEBUG%"=="" @echo off
@rem Gradle wrapper script - Android Studio will download the actual wrapper jar

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

where gradle >nul 2>nul
if %ERRORLEVEL% equ 0 (
    gradle %*
    goto :eof
)

echo gradle command not found.
echo Please open this project in Android Studio to sync gradle.

if "%OS%"=="Windows_NT" endlocal
