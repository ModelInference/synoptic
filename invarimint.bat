@echo off


REM Runs InvariMint from the compiled class files, passing all command

REM line argument directly to main().


java -ea -cp "./synoptic/bin/;./InvariMint/bin/;./daikonizer/bin/;./lib/*" main.InvariMintMain %*
