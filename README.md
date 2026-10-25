# Complicated Bees: Genetic Industry, unofficial NeoForge 26.x port

This branch (`port/26.x`) ports TheSmartNoob880's [Complicated Bees: Genetic Industry](https://github.com/TheSmartNoob880/Complicated-Bees-Genetic-Industry) 1.0.0 to Minecraft Java **26.1.2** and **26.2** on NeoForge. It is not an official upstream release. The original README and credits are kept as [README.txt](README.txt) and [CREDITS.txt](CREDITS.txt).

The branch starts at upstream `master` revision `68fceb01e926f18de1a76591b8691bdb75353bd9`, so GitHub's compare view shows exactly what the port changed.

| Minecraft | NeoForge tested | Java | Optional JEI tested | Optional Jade tested |
| --- | --- | --- | --- | --- |
| 26.1.2 | 26.1.2.122 | 25 | 29.47.0.111 | 26.1.12+neoforge |
| 26.2 | 26.2.0.93 | 25 | 30.42.0.237 | 26.2.10+neoforge |

Genetic Industry requires Complicated Bees. Use it with the Complicated Bees 26.x port for the same Minecraft version: [eriritarded/complicated_bees](https://github.com/eriritarded/complicated_bees), branch `port/26.x`. JEI and Jade are optional and are not bundled.

## Complicated Bees dependency

The build compiles (and the dev runs load) against the Complicated Bees port **jar** for the target being built. It looks for exactly one `complicated_bees-neoforge-*.jar` in `libs/<minecraft>/` (git-ignored), or takes an explicit path with `-PcbJar=<path>`. Without it the build stops and says what to do.

Get the jar one of these ways:

1. Let the build script build it. Clone the Complicated Bees fork next to this one and pass its path:

   ```powershell
   git clone -b port/26.x https://github.com/eriritarded/complicated_bees ..\complicated_bees
   .\build-port.ps1 -CbRepo ..\complicated_bees
   ```

   This builds Complicated Bees for each target and copies its jar into `libs/<minecraft>/` before building this mod.

2. Build Complicated Bees yourself (`.\build-port.ps1` in that clone) and copy `build/<minecraft>/libs/complicated_bees-neoforge-3.6.2+<minecraft>-port.2.jar` into `libs/<minecraft>/` here.

## Build

Install Java 25, then run from PowerShell:

```powershell
.\build-port.ps1                # both targets (jars must already be in libs/)
.\build-port.ps1 -Target 26.2   # one target
```

Or call Gradle directly, one target per invocation, keeping a separate project cache per target:

```sh
./gradlew build -PmcTarget=26.1.2 --project-cache-dir .gradle/26.1.2
```

Gradle 9.2.1 and ModDevGradle 2.0.148 are pinned. The jar appears in `build/<minecraft>/libs/genindustry_bees-neoforge-1.0.0+<minecraft>-port.2.jar`. Add `-PwithJei` / `-PwithJade` to put those mods on the `runClient` / `runServer` classpath.

## What changed

* Sources under `src/main` are adapted to native 26.x APIs and to the Complicated Bees 26.x port: NeoForge lifecycle and registries, genetic data in native `CUSTOM_DATA`, native recipe matching and bee components, native menu registration, component-aware tints and model properties.
* The 26.1.2 recipe-sync disconnect fix (`RecipeGeneticTemplate` codecs sharing one stateless instance) is included; see PORT_STATUS.md.
* `neoforge.mods.toml` uses `GPL-3.0-only`, matching the repository's LICENSE (the original Gradle metadata said LGPL 3.0).

## Validation

`src/validation` is a separate validation mod that exercises this mod and Complicated Bees together on a real dedicated server (130 assertions per target) and a connected client (7 assertions plus five screenshots per target). It is compiled only with `-PvalidatePort` or `-PclientValidation` and never ships in the production jar.

```powershell
.\build-port.ps1 -Validate
```

This prepares and runs isolated dedicated-server fixtures under `run/<minecraft>/server/` (it accepts the Minecraft EULA for those local test instances, binds to localhost, and uses ports 25661 and 25662). A run passes only if its fresh `validation-report.txt` starts with `PASS`.

For the connected-client checks, prepare the target, start the server and wait for `BEES_VALIDATION_PASS`, then start the client in another terminal:

```powershell
.\tools\prepare-validation.ps1 -Target 26.2
.\gradlew.bat runServer '-PmcTarget=26.2' '-PclientValidation' '-PwithJade' --project-cache-dir .gradle/26.2 --console=plain
.\gradlew.bat runClient '-PmcTarget=26.2' '-PclientValidation' '-PwithJei' '-PwithJade' --project-cache-dir .gradle/26.2 --console=plain
```

The recorded reports and screenshots from the original runs are in `evidence/26.1.2` and `evidence/26.2`. See [PORT_STATUS.md](PORT_STATUS.md) for what was and was not verified; complete gameplay parity is not claimed. `tools/README.md` describes the other scripts.

## Credits and licence

Genetic Industry is by TheSmartNoob880, inspired by Gendustry; thanks to Emperdog for code help and Filostorm for most of the textures (see CREDITS.txt). It is an addon for ACCBDD's Complicated Bees. The repository is licensed GPL-3.0 ([LICENSE.txt](LICENSE.txt)); the port's changes are offered under the same licence.
