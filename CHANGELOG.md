# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Renamed commands, gamerules, and config keys to a unified, self-explanatory naming convention (see `docs/HowItWorks.md` → "Naming Convention"). Re:Zero terminology ("Subaru", "RBD") no longer appears in any identifier or user-facing message.
- Commands:
    - `/setSubaruPlayer` → `/setanchor`
    - `/triggerRBD` → `/restorecheckpoint`
    - `/setcheckpoint` is unchanged.
- Gamerules (constants in `ModGameRules.java` now mirror the registered keys):
    - `checkpointFixedInterval` → `autoCheckpointIntervalSeconds`
    - `useRandomCheckpointInterval` → `autoCheckpointRandomIntervalEnabled`
    - `randomCheckpointLowerBound` → `autoCheckpointRandomMinSeconds`
    - `randomCheckpointUpperBound` → `autoCheckpointRandomMaxSeconds`
    - `fluteCooldownEnabled` → `artifactFluteCooldownEnabled`
    - `fluteCooldownDuration` → `artifactFluteCooldownSeconds`
    - `setCheckpointOnWorldCreation` → `checkpointOnWorldCreation`
    - `autoCheckpointEnabled` and `artifactFluteEnabled` are unchanged.
- Config (`config/minezero.cfg`): category `general`, key `deathChime` (`CLASSIC`/`ALTERNATE`) → category `sounds`, key `restoreSound` (`death_chime`/`alt_death_chime`, matching the mod's sound IDs). The empty `checkpoints` category was removed.
- Every gamerule now has a display name and description in the gamerule edit screen (`en_us.json`).

### Backward compatibility
- Commands: the old names `/setSubaruPlayer` and `/triggerRBD` remain registered as deprecated aliases for one release cycle and will then be removed.
- Gamerules: hard rename, no aliases — Forge gamerules are independent registrations, so an "alias" would be a second rule whose value silently diverges from the real one. Existing worlds keep the old entries in level.dat harmlessly; the renamed rules start at their defaults, so re-apply any non-default values with `/gamerule` after updating.
- Config: hard rename — Forge regenerates `minezero.cfg` with the new `sounds.restoreSound` key at its default. The legacy values `CLASSIC`/`ALTERNATE` are still accepted if set manually.

### Added
- Initial release of MineZero.
- Checkpoint system that saves player and world state.
- Anchor player system to trigger world resets.
- `/setcheckpoint` command for manual checkpoint creation.
- Artifact Flute item for instant checkpoint setting.
- Custom `death_chime` sound on world reset.
- Support for multiple Minecraft versions and mod loaders:
    - `1.20.1-forge`
    - `1.21.1-neoforge`
    - `1.20.1-fabric`
    - `1.12.2-forge`