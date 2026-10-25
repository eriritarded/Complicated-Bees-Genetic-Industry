# Port coverage and validation

> Split note (2026-10-25): this file was split out of the combined Complicated Bees + Genetic Industry port. Rows and sections that concern only the base mod are in the [Complicated Bees fork](https://github.com/eriritarded/complicated_bees/blob/port/26.x/PORT_STATUS.md). The validation harness in `src/validation` covers both mods together. Release archives under `dist/` mentioned below were build outputs of the combined repository and are not committed here; they are reproduced by building this branch.

## Recipe-sync hotfix, 2026-10-18

Genetic Industry **26.1.2 port.2** fixes the user-observed local-world disconnect while encoding `neoforge:recipe_content` for `RecipeGeneticTemplate`. The JSON and stream unit codecs now share one stateless recipe instance. Matching, gene data, recipe ID and zero-byte payload format remain. The source and historical generator are corrected; the shared26.2 source is corrected but no replacement26.2 binary was built here.

Use `dist/neoforge-26.1.2/genindustry_bees-neoforge-1.0.0+26.1.2-port.2.jar` with the unchanged base Complicated Bees port.1. This standalone replacement was compiled/inspected and installed in Simply NeoForged, with the old addon backed up. Only `recipe/modRecipes.class` and addon version metadata change; all other archive entries are byte-identical. The modified class was compiled against the exact installed26.1.2.122 profile using Java25, with annotation processing/implicit source compilation disabled. No game or tests were launched and replacement login remains unverified.

See `evidence/recipe-sync-hotfix-26.1.2.json`, `dist/hotfix-manifest.json` and `dist/SHA256SUMS-hotfix.txt`. The original bundles/source ZIP and validation reports below belong to port.1 and do not certify the hotfix. Build metadata now advances the addon to port.2 while retaining the base mod at port.1. The ordinary full-release packager still requires fresh matching tested bytes; it was not run for this narrow hotfix.


The runtime ports target Minecraft Java **26.1.2 / NeoForge 26.1.2.122** and **26.2 / NeoForge 26.2.0.93**, with Java 25, Gradle 9.2.1, ModDevGradle 2.0.148 and official Minecraft names. Loader versions were resolved from NeoForged Maven metadata on October 17, 2026, America/Indianapolis.

The clean source baselines are Complicated Bees 3.6.2, `1.21.1` revision `68d1299ef688ff9a844c5e16b37876c5122712b5` (ACCBDD, MIT), and Genetic Industry 1.0.0, `master` revision `68fceb01e926f18de1a76591b8691bdb75353bd9` (TheSmartNoob880, upstream GPL-3.0 LICENSE; conflicting LGPL metadata is recorded in README).

## Coverage

`evidence/source-coverage.csv` maps all **34 upstream Genetic Industry Java files** (375 across both mods) to their retained, adapted or inactive target source. Its file counts describe source coverage, not behavioral parity. No upstream Java file is unmapped.

| Source system / pointers | Target implementation | Status | Evidence and limits |
| --- | --- | --- | --- |
| Genetic Industry extraction, templates, imprinting | NeoForge lifecycle/registries; genetic data in native CUSTOM_DATA; native recipe matching and bee components | adapted | Every default gene sample codec and template insertion passes; blank/unrelated recipes are rejected. Registered tickers extract a gene and imprint both chromosomes; consumption, template retention and labware damage pass. |
| Genetic Industry menus, gene tint/template model property, recycling | Native menu registration and component-aware tint/range property; component-sensitive smelting subclass | adapted | Both addon menus open over actual client/server packets. JEI plugin registration succeeds. Every upgrade/smelting-input combination is unverified. |
| JEI and Jade | Pinned target APIs; native recipe display and tooltip providers | adapted | Both mod JEI plugins and Complicated Bees' Jade plugin initialize in the connected runs. Exhaustive category navigation and tooltip content are unverified. |

## Checks performed

Each target has its own real dedicated-server fixture and connected Minecraft client. The final server suite contains **130 passing assertions per target**. The client suite contains **7 passing assertions per target**, plus five captured images reviewed for rendering/layout. The reports list the exact assertions; a large part of the count comes from per-gene and per-block checks. The fifth image shows the apiary in the world with Jade's block tooltip.

The server fixture runs registered block tickers synchronously to accelerate processing, explicitly initializes the queued apiary load callback, and invokes the normal final-block placement callback for multiblocks. This verifies those paths and their state changes; it does not represent normal elapsed game time or a human placing every block. The client uses the real multiplayer block-interaction and menu-packet path.

Final per-target evidence is stored in `evidence/26.1.2/` and `evidence/26.2/`: server/client reports, the item gallery, three menu captures, and production-output hashes. The final client runs include JEI 29.47.0.111 / 30.42.0.237 and Jade 26.1.12+neoforge / 26.2.10+neoforge respectively. Optional mods are not bundled.

Failures encountered and corrected during this port included registration before native property IDs were bound, legacy recipe/ingredient formats, early product decoding before item components were ready, unsupported 26.2 tree data, client toast classes referenced during dedicated-server login, the deprecated 26.2 mod-logo metadata warning, and Jade rejecting a combined server/client provider. Jade's data provider is now separate from its client tooltip provider. Fixture-only failures included a falling player outside menu reach and a synchronously tested apiary whose normal queued load callback had not yet run; those fixtures were corrected. Final reports supersede the earlier failed runs. Windows OSHI/Perflib counter errors in test logs were host diagnostics; no registry changes were made.

## Artifact identity and limits

Ordinary production builds exclude the separate validation mod, its classes, resources and target adapters. The release packaging script compares every shipped production class/resource with hashes captured from the tested development output, checks target metadata and Java classfile major 69, parses packaged JSON as UTF-8, and checks PNG chunks/CRCs/compression. Its actual result and counts are in `dist/package-validation.json`; artifact sizes and SHA-256 hashes are in `dist/manifest.json` and `dist/SHA256SUMS.txt`.

Gameplay tests use the ModDevGradle development runtime. Matching official-name production bytes relate those checks to the delivered JARs; a launcher-installed production-JAR session was not run. Gradle unit-test tasks report `NO-SOURCE` and are not counted as tests. Existing-world migration, arbitrary datapacks/modpacks, natural generation distribution, long-running balance, every screen/upgrade/effect, broad automation interoperability, audio quality, WAN and multi-player load remain unverified. Complete behavioral parity is not claimed.

No game installation, launcher edit, publication, or player-world modification was performed. Development worlds and caches are isolated and excluded from the source/release bundles. The source bundle includes the port, build inputs, original licenses, inactive historical sources, validation harness and evidence.
