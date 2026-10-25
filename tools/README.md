`prepare-validation.ps1` configures only this repository's ignored, isolated test instances under `run/<minecraft>/`. `build-port.ps1 -Validate` calls it.

The remaining scripts come from the combined Complicated Bees + Genetic Industry port (the private working repository this fork was split from) and still expect that repository's layout (`complicated_bees/`, `genetic_industry/`, `dist/`, `reference/` checkouts):

* `capture_tested_output.py` recorded production byte hashes after the validation reports passed, and `package_release.py` checked the final jars of both mods against those hashes and packaged the release bundles. They are kept as a record of how the published port.1 bundles were checked and have not been adapted to the split repositories.
* `migrate_addon.py`, `rewrite_addon_core.py`, `migrate_resources.py` and `prepare_port.py` are historical, one-time migration notes. They must not be rerun on the finished source. `migrate_resources.py` and `prepare_port.py` worked on both mods and are copied into both forks.

Normal builds use Gradle and do not execute any of them.
