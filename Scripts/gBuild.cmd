@ECHO off
IF "%JAVA_HOME%"=="" (
    ECHO The JAVA_HOME environment variable must be set for gBuild to work.
    ECHO Note: JAVA_HOME must point to a jdk
    SET ERROR_CODE=1
    exit
)

SET "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
SET "FOUND_BIN=0"
FOR %%G IN ("%path:;=";"%") DO (
    IF exist "%%~fG\gBuild*.jar" (
        SET "FOUND_BIN=1"
        For /F %%H IN ('"dir /s /b %%~fG\gBuild*.jar"') DO (
            SET BIN_NAME="%%H"
        )
    )
)

IF "%FOUND_BIN%" == "0" (
    ECHO Could not find a gBuild binary
    SET "ERROR_CODE=1"
    exit
)

"%JAVA_BIN%" -jar "%BIN_NAME%" %1 %2 %3
SET "ERROR_CODE=0"