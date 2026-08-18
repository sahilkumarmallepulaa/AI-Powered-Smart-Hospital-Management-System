@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@if "%DEBUG%" == "" @echo off
@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR%" == "" set MAVEN_PROJECTBASEDIR=.

set MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set MAVEN_WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

@REM Find JAVA_HOME
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto error

:init
@REM Find the project base dir, i.e. the dir that contains the folder ".mvn".
@REM Fallback to current dir if not found.

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR%" == "" set MAVEN_PROJECTBASEDIR=.

:findBaseDir
if exist "%MAVEN_PROJECTBASEDIR%\.mvn" goto baseDirFound
cd ..
if "%cd%" == "%MAVEN_PROJECTBASEDIR%" goto baseDirNotFound
set MAVEN_PROJECTBASEDIR=%cd%
goto findBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%~dp0

:baseDirFound
cd "%MAVEN_PROJECTBASEDIR%"

@REM Provide a way to customize the wrapper jar location/name
if not "%WRAPPER_JAR%" == "" set MAVEN_WRAPPER_JAR="%WRAPPER_JAR%"

@REM Provide a way to customize the wrapper properties location/name
if not "%WRAPPER_PROPERTIES%" == "" set MAVEN_WRAPPER_PROPERTIES="%WRAPPER_PROPERTIES%"

set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@REM Execute Maven Wrapper
"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -classpath %MAVEN_WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
