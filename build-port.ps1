param(
    [ValidateSet('all','26.1.2','26.2')][string]$Target = 'all',
    # Optional: path to a clone of eriritarded/complicated_bees (branch port/26.x). When given, that
    # project is built for each target first and its jar is copied into libs/<minecraft>/.
    [string]$CbRepo,
    [switch]$Validate
)
$ErrorActionPreference = 'Stop'
$portTargets = if ($Target -eq 'all') { @('26.1.2','26.2') } else { @($Target) }
Push-Location $PSScriptRoot
try {
    foreach ($portTarget in $portTargets) {
        if ($CbRepo) {
            & (Join-Path $CbRepo 'build-port.ps1') -Target $portTarget
            if ($LASTEXITCODE -ne 0) { throw "Complicated Bees build failed for $portTarget" }
            $libDir = Join-Path $PSScriptRoot "libs/$portTarget"
            New-Item -ItemType Directory -Force -Path $libDir | Out-Null
            Get-ChildItem -LiteralPath $libDir -Filter 'complicated_bees-neoforge-*.jar' | Remove-Item
            Get-ChildItem -LiteralPath (Join-Path $CbRepo "build/$portTarget/libs") -Filter 'complicated_bees-neoforge-*.jar' |
                Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' } | Copy-Item -Destination $libDir
        }
        # Each Minecraft target gets its own project cache so the two NeoForge setups never collide.
        $portArgs = @('build', "-PmcTarget=$portTarget", '--project-cache-dir', "$PSScriptRoot/.gradle/$portTarget", '--console=plain')
        & "$PSScriptRoot/gradlew.bat" @portArgs
        if ($LASTEXITCODE -ne 0) { throw "Build failed for $portTarget" }
        if ($Validate) {
            & "$PSScriptRoot/tools/prepare-validation.ps1" -Target $portTarget
            $portReport = Join-Path $PSScriptRoot "run/$portTarget/server/validation-report.txt"
            if (Test-Path -LiteralPath $portReport) { Remove-Item -LiteralPath $portReport }
            & "$PSScriptRoot/gradlew.bat" 'runServer' "-PmcTarget=$portTarget" '-PvalidatePort' '--project-cache-dir' "$PSScriptRoot/.gradle/$portTarget" '--console=plain'
            if ($LASTEXITCODE -ne 0) { throw "Validation launch failed for $portTarget" }
            if (!(Test-Path -LiteralPath $portReport) -or !(Get-Content -LiteralPath $portReport -First 1).StartsWith('PASS ')) { throw "Validation did not pass for $portTarget; inspect $portReport" }
        }
    }
} finally { Pop-Location }
