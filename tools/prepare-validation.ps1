param([ValidateSet('26.1.2','26.2')][string]$Target = '26.1.2')
$ErrorActionPreference = 'Stop'
$portRoot = Split-Path $PSScriptRoot -Parent
$portRun = Join-Path $portRoot "run/$Target/server"
New-Item -ItemType Directory -Force -Path $portRun | Out-Null
# These are isolated local test instances, never a launcher profile or an existing player world.
'eula=true' | Set-Content -Encoding utf8 -LiteralPath (Join-Path $portRun 'eula.txt')
$portNumber = if ($Target -eq '26.1.2') { 25661 } else { 25662 }
@"
server-ip=127.0.0.1
server-port=$portNumber
online-mode=false
level-name=world
level-type=minecraft:flat
generator-settings={"biome":"minecraft:plains","layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"features":false,"lakes":false}
view-distance=3
simulation-distance=3
spawn-protection=0
max-players=2
"@ | Set-Content -Encoding utf8 -LiteralPath (Join-Path $portRun 'server.properties')
$portClient = Join-Path $portRoot "run/$Target/client"
New-Item -ItemType Directory -Force -Path $portClient | Out-Null
@'
onboardAccessibility:false
guiScale:2
pauseOnLostFocus:false
fullscreen:false
renderDistance:3
simulationDistance:5
maxFps:30
enableVsync:false
'@ | Set-Content -Encoding utf8 -LiteralPath (Join-Path $portClient 'options.txt')
