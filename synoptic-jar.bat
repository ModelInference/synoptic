@echo off

REM Runs Synoptic from the compiled JAR file, passing all command
REM line argument directly to main().

java -ea -jar ./lib/synoptic.jar %*
