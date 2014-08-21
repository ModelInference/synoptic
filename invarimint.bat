@echo off


REM Runs InvariMint from the compiled class files, passing all command

REM line argument directly to main().


java -ea -cp ./lib/junit-4.9b2.jar;./lib/automaton.jar;./lib/plume.jar;./lib/synoptic.jar;./InvariMint/bin/ main.InvariMintMain %*
