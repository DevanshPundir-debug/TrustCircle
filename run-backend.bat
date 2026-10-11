@echo off
REM Start the Trust Circle backend on http://localhost:8085
cd /d "%~dp0"

if not exist classpath.txt (
    echo classpath.txt is missing. Regenerate it with:
    echo   mvnw.cmd dependency:build-classpath -Dmdep.outputFile=classpath.txt
    exit /b 1
)

set /p DEPS=<classpath.txt
java -cp "target\classes;%DEPS%" TrustCircle.Project.ProjectApplication
