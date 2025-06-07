[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$InputFile,
    [string]$Structure = "",
    [string]$Output = ""
)

if ([string]::IsNullOrWhiteSpace($InputFile)) {
    Write-Host "Usage: .\pre-parser.ps1 -inputFile <file.pdf|.txt> [-structure <structure.json>] [-output <output.json>]" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path -LiteralPath $InputFile)) {
    Write-Host "Error: Input file '$InputFile' does not exist." -ForegroundColor Red
    exit 1
}

$BaseName      = Split-Path -Leaf $InputFile
$BaseNameNoExt = [System.IO.Path]::GetFileNameWithoutExtension($BaseName)

if ([string]::IsNullOrWhiteSpace($Structure)) {
    $Structure = "$BaseNameNoExt-structure.json"
}

if ([string]::IsNullOrWhiteSpace($Output)) {
    $Output = "$BaseNameNoExt-data.json"
}

Write-Host "InputFile: $InputFile"
Write-Host "Structure: $Structure"
Write-Host "Output: $Output"

$JarPath = Join-Path -Path (Split-Path -Parent $MyInvocation.MyCommand.Path) -ChildPath "target\pre-parser-0.1-SNAPSHOT.jar"

if (-not (Test-Path -LiteralPath $JarPath)) {
    Write-Host "Error: JAR not found at '$JarPath'." -ForegroundColor Red
    exit 1
}

Write-Host "Running: java -jar `"$JarPath`" `"$InputFile`" `"$Structure`" `"$Output`""
& java -jar "$JarPath" "$InputFile" "$Structure" "$Output"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Java process exited with code $LASTEXITCODE." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Process completed successfully." -ForegroundColor Green
