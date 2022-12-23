Rem legacy @start /b cmd /c gradle runWebserver
Rem legacy @start /b cmd /c gradle runWebsocket
set javaClasspath=%1
set javaMainClass=%2
set javascriptServerPath=%3

@start /b cmd /c java -cp "%javaClasspath%" %javaMainClass%
@start /b cmd /c node %javascriptServerPath%
