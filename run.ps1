# Self-contained JavaFX Runner Script for Windows
$ProjectDir = Get-Location
$MvnDir = "$ProjectDir\.maven"
$ZipFile = "$ProjectDir\maven.zip"
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"

Clear-Host
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "         LICENSIFY - APPLICATION LAUNCHER" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Ensure Maven is available locally
if (!(Test-Path "$MvnDir\apache-maven-3.9.6")) {
    Write-Host "[-] Maven not detected locally." -ForegroundColor Yellow
    Write-Host "[*] Downloading portable Maven distribution (~10MB)..." -ForegroundColor Gray
    
    try {
        Invoke-WebRequest -Uri $MavenUrl -OutFile $ZipFile
        Write-Host "[*] Extracting Maven..." -ForegroundColor Gray
        Expand-Archive -Path $ZipFile -DestinationPath $MvnDir
        Remove-Item $ZipFile
        Write-Host "[+] Maven successfully configured!" -ForegroundColor Green
    } catch {
        Write-Host "[!] Error downloading Maven: $_" -ForegroundColor Red
        Write-Host "[!] Please ensure you have an active internet connection." -ForegroundColor Red
        Exit
    }
} else {
    Write-Host "[+] Local Maven environment detected." -ForegroundColor Green
}

# 2. Run the application
Write-Host "[*] Starting the JavaFX application..." -ForegroundColor Gray
$MvnCmd = "$MvnDir\apache-maven-3.9.6\bin\mvn.cmd"

& $MvnCmd clean javafx:run

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Application terminated." -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
