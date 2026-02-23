# PowerShell script to clean all Gradle caches
# Run this script to remove cached Kotlin stdlib artifacts

Write-Host "Cleaning Gradle caches..." -ForegroundColor Yellow

$gradleUserHome = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { "$env:USERPROFILE\.gradle" }

# Clean project build directories
Write-Host "Cleaning project build directories..." -ForegroundColor Cyan
if (Test-Path ".\build") { Remove-Item -Recurse -Force ".\build" }
if (Test-Path ".\app\build") { Remove-Item -Recurse -Force ".\app\build" }
if (Test-Path "\.gradle") { Remove-Item -Recurse -Force "\.gradle" }

# Clean Gradle user home caches
Write-Host "Cleaning Gradle user home caches..." -ForegroundColor Cyan
if (Test-Path "$gradleUserHome\caches") {
    # Remove Kotlin stdlib specific caches
    $kotlinCachePaths = @(
        "$gradleUserHome\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib-jdk7\1.7.20",
        "$gradleUserHome\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib\1.7.20",
        "$gradleUserHome\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib-jdk8\1.7.20"
    )
    
    foreach ($path in $kotlinCachePaths) {
        if (Test-Path $path) {
            Write-Host "Removing: $path" -ForegroundColor Red
            Remove-Item -Recurse -Force $path -ErrorAction SilentlyContinue
        }
    }
    
    # Clean transform cache (where the error occurs)
    if (Test-Path "$gradleUserHome\caches\transforms-3") {
        Write-Host "Cleaning transform cache..." -ForegroundColor Cyan
        Remove-Item -Recurse -Force "$gradleUserHome\caches\transforms-3" -ErrorAction SilentlyContinue
    }
    
    # Clean build cache
    if (Test-Path "$gradleUserHome\caches\build-cache-1") {
        Write-Host "Cleaning build cache..." -ForegroundColor Cyan
        Remove-Item -Recurse -Force "$gradleUserHome\caches\build-cache-1" -ErrorAction SilentlyContinue
    }
}

Write-Host "`nCache cleanup complete!" -ForegroundColor Green
Write-Host "Please sync your project in Android Studio and rebuild." -ForegroundColor Yellow
