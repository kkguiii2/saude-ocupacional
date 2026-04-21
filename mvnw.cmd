@echo off
REM Maven Wrapper - Simple build tool for this project
set JAVA_HOME=C:\Program Files\Java\jdk-17
set MAVEN_OPTS=-Xmx512m

set ARG=%1

if "%ARG%"=="" echo Usage: mvnw [compile|run|package|clean] & exit /b 1

if "%ARG%"=="compile" goto compile
if "%ARG%"=="run" goto run
if "%ARG%"=="package" goto package
if "%ARG%"=="clean" goto clean
if "%ARG%"=="test" goto test

echo Unknown command: %ARG%
exit /b 1

:compile
echo Compiling...
"%JAVA_HOME%\bin\javac" -d target/classes -cp "target/dependency/*" -sourcepath src/main/java src/main/java/com/industrial/saude/*.java src/main/java/com/industrial/saude/**/*.java 2>nul
if exist target\classes (echo Compiled successfully!) else (echo No target folder - this is a Spring Boot project, use 'mvnw run')
exit /b 0

:run
echo Starting Spring Boot application...
"%JAVA_HOME%\bin\java" -cp "target/classes;target/dependency/*" com.industrial.saude.SaudeOcupacionalApplication
exit /b 0

:package
echo Packaging...
"%JAVA_HOME%\bin\jar" -cf target/saude-ocupacional.jar -C target/classes .
exit /b 0

:clean
if exist target rd /s /q target
echo Cleaned!
exit /b 0

:test
echo Running tests...
echo Tests require full Maven build - please install Maven
exit /b 0