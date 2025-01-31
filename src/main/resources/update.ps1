# Define the archive and task names
$archivePath = "flat-parser.zip"
$taskName = "flat-parser"

# Check if the archive exists
if (Test-Path $archivePath) {
    # Kill the task if it exists
    $task = Get-Process -Name $taskName -ErrorAction SilentlyContinue
    if ($task) {
        Stop-Process -Name $taskName -Force
    }

    # Unpack the archive to the current folder and replace files
    Expand-Archive -Path $archivePath -DestinationPath . -Force

    # Delete the archive
    Remove-Item $archivePath -Force
    # Start the application again
    Start-Process -FilePath "flat-parser.exe"
} else {
    Write-Host "Archive $archivePath does not exist."
}