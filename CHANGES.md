# Changes from Shattered Pixel Dungeon v3.3.8

Upstream history is retained. Work branch: `grimhollow`.

- `.gitignore`: exclude local toolchains, build caches, temporary credentials and output.
- `tools/bootstrap-windows.ps1`: install checksum-verified Temurin 17 and Android tools into the repository's ignored local toolchain directory.
- `tools/env.ps1`: configure the local build session without changing system settings.
- `KNOWN_ISSUES.md`: record specification conflicts, environment failures, and acceptance status.
- `CHANGES.md`: maintain this per-file change ledger.
