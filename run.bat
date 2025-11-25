@echo off
REM Download MySQL JDBC Driver from alternative source
cd /d "%~dp0"

REM Try downloading from jcenter
powershell -Command "try { (New-Object System.Net.ServicePointManager).SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://jcenter.bintray.com/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar' -OutFile 'mysql-connector-java-8.0.33.jar'; Write-Host 'Downloaded successfully from jcenter' } catch { Write-Host 'Failed: ' $_.Exception.Message }"

REM Check if download succeeded
if exist mysql-connector-java-8.0.33.jar (
    echo Driver downloaded successfully!
    echo Compiling GUI...
    javac BankSystemGUI.java
    echo Starting application...
    java -cp ".;mysql-connector-java-8.0.33.jar" BankSystemGUI
) else (
    echo Failed to download driver. Trying embedded driver approach...
    javac BankSystemGUI.java
    java -cp "." BankSystemGUI
)
