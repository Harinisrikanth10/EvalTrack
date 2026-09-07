# PowerShell script for Maven Wrapper
$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$wrapperJar = Join-Path $scriptDir ".mvn\wrapper\maven-wrapper.jar"
java "-Dmaven.multiModuleProjectDirectory=$scriptDir" -classpath "$wrapperJar" org.apache.maven.wrapper.MavenWrapperMain @args
