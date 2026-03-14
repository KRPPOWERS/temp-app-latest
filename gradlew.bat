@rem Gradle startup script for Windows
@echo off
set APP_HOME=%~dp0
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute
echo ERROR: JAVA_HOME is not set correctly.
exit /B 1
:execute
"%JAVA_EXE%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
