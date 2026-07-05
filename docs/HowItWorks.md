# MineZero: How It Works

This document explains the design and inner workings of MineZero's checkpoint and world reset system so contributors and advanced users can understand what happens under the hood.

---

## High-level Flow

- A checkpoint can be created by:
  - Using the `/setcheckpoint` command.
  - Using the `Artifact Flute` item.
  - Automatic intervals driven by gamerules.
- The checkpoint captures:
  - Player states (position, dimension, inventory, health/hunger/XP, potion effects, game mode, respawn point, motion, fall distance, advancements).
  - World time and weather (daytime, game time, raining/thundering, clear weather time).
  - Block changes, block entities, and tracked differences in chunks.
  - Entities and items on the ground, across dimensions, plus mob aggro targets.
- When the anchor player dies (or a restore is manually triggered), MineZero restores the saved checkpoint across players, world time/weather, blocks, entities, and items.

---

## Key Concepts

- Anchor Player: The UUID of the player whose death triggers the reset. Set when a checkpoint is created, or via command if needed.
- Checkpoint Data: A SavedData entry (`global_checkpoint`) persisted in the overworld's data storage. It holds players, entities, world state, and world diffs.
- World Diffs: Rather than snapshotting entire worlds, MineZero stores changed blocks and block entities, keyed by dimension and chunk, and replays them on restore.

---

## Core Components

- Checkpoint orchestration: `checkpoint/CheckpointManager.java`
  - `setCheckpoint(ServerPlayer)` gathers and saves world and player data.
  - `restoreCheckpoint(ServerPlayer)` replays the saved state.
- Persistent storage: `checkpoint/CheckpointData.java`
  - Stores player NBT, entities, ground items, and `WorldData` (time/weather/blocks/etc.).
  - Serialized via NBT and saved under `global_checkpoint`.
- World diffs: `checkpoint/WorldData.java`
  - Tracks modified/mined blocks, fluids, block entities, lightning strikes, eyes of ender additions, and more, grouped by chunk and dimension.
- Event triggers:
  - `event/DeathEventHandler.java`: Cancels death of the anchor player and schedules a restore (Return By Death).
  - `event/CheckpointTicker.java`: Periodic auto-checkpoints via gamerules (fixed or random intervals). Also assigns an anchor if missing.
  - `event/ExplosionEventHandler.java` and `event/NonPlayerChangeHandler.java`: Integrations to keep diffs consistent around explosions and non-player changes.
- Commands and items:
  - `/setcheckpoint`: `command/SetCheckpointCommand.java`.
  - `/setanchor`: set anchor explicitly, `command/SetAnchorCommand.java`.
  - `/restorecheckpoint`: manually trigger restore, `command/RestoreCheckpointCommand.java`.
  - Artifact Flute: `items/ArtifactFluteItem.java` calls `setCheckpoint` and enforces gamerules like cooldown/enable.

---

## What gets saved in a checkpoint

- Players:
  - Position, rotation, dimension
  - Game mode
  - Motion vector, fall distance
  - Health, hunger, XP level/progress
  - Respawn point (coords, dimension, forced)
  - Active potion effects
  - Inventory contents (entire container)
  - Advancements progress (per-criterion)
- World:
  - Daytime and game time
  - Weather flags and timers (raining, thundering, clear weather time)
  - Modified/mined blocks, tracked by dimension and chunk
  - Block entity NBT per position
  - Fluid modifications (added/removed)
  - Eyes of Ender added to end portal frames
  - Scheduled lightning strikes
- Entities:
  - Mobs and select non-player entities (boats, minecarts, items, TNT, etc.) across all loaded dimensions
  - Mob aggro target relationships
  - Items on the ground in the overworld

---

## Restore sequence (Return By Death)

1. Restore time and weather on the server and broadcast rain/thunder state to clients.
2. Revert modified blocks to air and restore mined blocks to their original states, by dimension.
3. Load block entity NBT back into the world.
4. For each player:
   - Teleport to saved dimension/position and orientation.
   - Restore health/hunger/XP, game mode, motion, fall distance, respawn point.
   - Clear and restore inventory.
   - Reconcile advancements to the saved progress (award/revoke criteria as needed).
5. Remove all non-player entities across all dimensions, then respawn saved entities and items, and re-assign mob aggro targets.
6. Undo specific world interactions (e.g., remove eyes placed in end frames since checkpoint, remove new fire blocks, replay scheduled lightning).

---

## Naming Convention

One convention covers commands, gamerules, and config keys across all branches:

- **Commands**: all lowercase, no separators, matching vanilla commands like `setworldspawn` — e.g. `setcheckpoint`, `setanchor`, `restorecheckpoint`. Command classes are named `<Command>Command` in UpperCamelCase, e.g. `SetAnchorCommand`.
- **Gamerules**: camelCase, matching vanilla gamerules like `doMobSpawning`. Feature toggles end in `Enabled` (`randomAnchorEnabled`); event-driven checkpoint triggers read `checkpointOn<Event>` (`checkpointOnSleep`); other booleans read as plain-English predicates (`anyPlayerDeathTriggersRestore`). Integer rules end with their unit (`autoCheckpointIntervalSeconds`).
- **Config keys**: camelCase, grouped by TOML section — e.g. `[sounds] restoreSound`. String values match in-game IDs (e.g. sound IDs like `death_chime`) rather than invented labels.
- **Java constants**: SCREAMING_SNAKE form of the registered key — key `randomAnchorEnabled` is constant `RANDOM_ANCHOR_ENABLED`.
- **Lang keys**: match registered identifiers exactly — `gamerule.randomAnchorEnabled` plus a `.description` entry.
- **User-facing text**: plain English; the death-triggering player is always the "anchor player". Re:Zero flavor (Subaru, Return By Death) stays in the mod description and store text only — never in commands, gamerules, config keys, or messages.

---

## Gamerules

Defined in `ModGameRules.java`:
- `autoCheckpointEnabled` (bool): enable periodic auto checkpoints.
- `autoCheckpointIntervalSeconds` (int, seconds): fixed interval if random disabled.
- `autoCheckpointRandomIntervalEnabled` (bool): whether to randomize the interval.
- `autoCheckpointRandomMinSeconds` / `autoCheckpointRandomMaxSeconds` (int, seconds): bounds for randomized interval.
- `artifactFluteCooldownEnabled` (bool) and `artifactFluteCooldownSeconds` (int, seconds): Artifact Flute cooldown.
- `artifactFluteEnabled` (bool): enable/disable the Artifact Flute entirely.
- `checkpointOnWorldCreation` (bool): set a checkpoint when the world is created.
- `randomAnchorEnabled` (bool): randomly pick a new anchor each automatic checkpoint.
- `anyPlayerDeathTriggersRestore` (bool): any player's death restores the checkpoint, not just the anchor's.
- `checkpointOnSleep` (bool): set a checkpoint when all players sleep and the night is skipped.

---

## Commands

- `/setcheckpoint [player]` (OP 2): Create a checkpoint for yourself or a target player; sets anchor to that player.
- `/setanchor <player>` (OP 2): Explicitly set the anchor player (does not save a checkpoint).
- `/restorecheckpoint` (OP 2): Manually restore the last checkpoint.

Deprecated aliases `/setSubaruPlayer` and `/triggerRBD` are still registered for one release cycle and will be removed; see CHANGELOG.

---

## Artifact Flute

- Right-click to immediately set a checkpoint server-side.
- Optional cooldown and enable/disable controlled by gamerules.
- Plays a custom sound and notifies the player on success.

---

## Design Notes

- Dimension-aware diffs: All saved block/entity operations are dimension-tagged to correctly replay across the nether/end/overworld.
- Minimal churn: Block updates use conservative flags to reduce neighbor updates and client lag when restoring.
- Safety: Restore runs on the server thread; non-player entities are discarded before respawn to avoid duplication.

---

## Extending MineZero

- Add more tracked world interactions in `WorldData` and wire event handlers to capture them.
- Extend saved player attributes in `PlayerData` and serialization in `CheckpointData`.
- Hook new triggers for setting checkpoints or restoring (e.g., boss kills, structures) via Forge events.
