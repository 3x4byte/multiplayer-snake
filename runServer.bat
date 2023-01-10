set javaClasspath=%1
set javaMainClass=%2
set javascriptServerPath=%3

@REM Windows
if "%OS%" == "Windows_NT" (
    @start /b cmd /c java -cp "%javaClasspath%" %javaMainClass%
    @start /b cmd /c node %javascriptServerPath%
    goto done @REM we need this because batch will execute any case in the if else
)

@REM Mac
if "%OSTYPE%"=="darwin" (
    goto done
)

@REM Linux
( java -cp "%javaClasspath%" %javaMainClass% ) &
( node  %javascriptServerPath% )  &


done:
