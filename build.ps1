# Builds the debug APK with the portable JDK 17 from .toolchain (if present).
$ErrorActionPreference = "Stop"

$jdkRoot = Join-Path $PSScriptRoot ".toolchain\jdk17"
if (Test-Path $jdkRoot) {
    $jdk = Get-ChildItem $jdkRoot -Directory | Select-Object -First 1
    if ($jdk) {
        $env:JAVA_HOME = $jdk.FullName
    }
}

if (-not $env:ANDROID_HOME) {
    $env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA "Android\Sdk"
}
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME

# Merge stderr into stdout: Gradle prints harmless warnings there, and PowerShell
# would otherwise treat them as terminating errors.
$ErrorActionPreference = "Continue"
& cmd /c "`"$PSScriptRoot\gradlew.bat`" :app:assembleDebug --no-daemon --console=plain 2>&1"
if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed with exit code $LASTEXITCODE"
}
