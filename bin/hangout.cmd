rem @echo off

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal
set basedir=%~dp0
set basedir=%BASE_DIR:~0,-1%
for %%d in (%basedir%) do set basedir=%%~dpd

set CLASSPATH=.;%JAVA_HOME%\lib;%JAVA_HOME%\lib\tools.jar

rem ===========================================================================================
rem  JVM Configuration
rem ===========================================================================================
set "JAVA_OPTS=%JAVA_OPTS% -server -Xms2g -Xmx2g -Xmn1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSParallelRemarkEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+CMSClassUnloadingEnabled -XX:SurvivorRatio=8 -XX:-UseParNewGC"
set "JAVA_OPTS=%JAVA_OPTS% -verbose:gc -Xloggc:"%USERPROFILE%\hangout.log" -XX:+PrintGCDetails"
set "JAVA_OPTS=%JAVA_OPTS% -XX:-OmitStackTraceInFastThrow"
set "JAVA_OPTS=%JAVA_OPTS% -XX:-UseLargePages"
set "JAVA_OPTS=%JAVA_OPTS% -Djava.ext.dirs=%basedir%libs;%basedir%modules"
set "JAVA_OPTS=%JAVA_OPTS% -cp %CLASSPATH%"
%JAVA% %JAVA_OPTS% com.ctrip.ops.sysdev.core.Main %*
