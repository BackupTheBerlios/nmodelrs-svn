@echo off
call env.bat
set JAVA_HOME=c:\java\jdk1.6.0_12
set ANT_HOME=c:\java\apache-ant-1.7.1
path=%ANT_HOME%\bin;%path%
%JAVA_HOME%\bin\java -version
call ant -version
rem set ANT_OPTS=-Xmx256m -XX:MaxPermSize=160m
rem echo ANT_OPTS=%ANT_OPTS%

