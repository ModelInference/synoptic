REM Runs Synoptic from the compiled JAR file, passing all command
REM line argument directly to main().

@echo off
java -ea -jar ./lib/synoptic.jar %*
