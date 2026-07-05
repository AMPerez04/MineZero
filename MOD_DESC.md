MineZero is a tactical checkpoint mod that allows players to set checkpoints and reset the world upon death. Inspired by _Re:Zero_, it works in both single-player and multiplayer. The last player to set a checkpoint becomes the "anchor," whose death resets the world and restores all players to their saved states, including inventory, health, hunger, fire ticks, and XP.

**Key Features:**

*   **Inspired by Re:Zero**: Tactical resets based on checkpoints just like Subaru Natsuki.
*   **Artifact Flute**: Set checkpoints instantly with a special item.
*   **Multiplayer Support**: Anchor system ensures a cooperative challenge.
*   **Entity and Item Restoration**: Resets all mobs, players, blocks, and ground items.
*   **Enhanced Aesthetic**: Uses textures from [unused-textures](https://github.com/malcolmriley/unused-textures).

Perfect for survival challenges, adventure maps, and roleplaying, MineZero offers a unique twist to your gameplay.

[![Join Discord](https://i.imgur.com/2F7fEU8.gif)](https://discord.gg/S9zrhP8e5B)

## Join My Discord for Feedback & Suggestions!

If you're enjoying my mod and want to help shape its future, I'd love to hear from you! Join my [Discord server](https://discord.gg/S9zrhP8e5B) to share feedback, report bugs, suggest new features, or discuss Minecraft modding. Your input is valuable and will directly influence the mod's development.

***

### Looking for More _Re:Zero_ in Minecraft?

If you're a fan of _Re:Zero_, you might want to check out [_Re:Zero Experience_](https://www.curseforge.com/minecraft/mc-mods/re-zero-experience) by Zoomerreid. It adds characters, magic, and abilities straight from the series, making for a pretty unique experience.

While _MineZero_ focuses on **Return by Death**, _Re:Zero Experience_ brings in **Witch Factors, Divine Protections, and battles against major enemies** from the anime. The mods aren’t officially integrated, but they both add different pieces of the _Re:Zero_ world to Minecraft. If you're into the series, it's worth a look.

## Commands

All commands require OP level 2.

*   `/setcheckpoint [player]` → Saves a checkpoint and sets the anchor player.
*   `/setanchor <player>` → Sets the anchor player only (does not save a checkpoint).
*   `/restorecheckpoint` → Manually restores the last checkpoint.

## Gamerules

### Automatic Checkpoints

*   **Usage:** `/gamerule autoCheckpointEnabled true`
*   **Description:** Enables automatic checkpoint creation. When set to true, checkpoints will be automatically created based on the configured intervals.

### Auto Checkpoint Interval (seconds)

*   **Usage:** `/gamerule autoCheckpointIntervalSeconds <seconds>`
*   **Description:** Sets the fixed interval (in seconds) between each checkpoint when not using random intervals.

### Use Random Checkpoint Interval

*   **Usage:** `/gamerule autoCheckpointRandomIntervalEnabled true`
*   **Description:** When enabled, the mod will choose a random interval for checkpoint creation instead of using a fixed value.

### Random Interval Minimum (seconds)

*   **Usage:** `/gamerule autoCheckpointRandomMinSeconds <seconds>`
*   **Description:** Defines the minimum number of seconds for the random checkpoint interval.

### Random Interval Maximum (seconds)

*   **Usage:** `/gamerule autoCheckpointRandomMaxSeconds <seconds>`
*   **Description:** Defines the maximum number of seconds for the random checkpoint interval.

### Artifact Flute Cooldown

*   **Usage:** `/gamerule artifactFluteCooldownEnabled true`
*   **Description:** Enables a cooldown period after using the Artifact Flute to prevent rapid re-use.

### Artifact Flute Cooldown (seconds)

*   **Usage:** `/gamerule artifactFluteCooldownSeconds <seconds>`
*   **Description:** Sets the duration (in seconds) of the cooldown period for the Artifact Flute.

### Artifact Flute Enabled

*   **Usage:** `/gamerule artifactFluteEnabled true`
*   **Description:** Enables or disables the Artifact Flute entirely.

### Checkpoint on World Creation

*   **Usage:** `/gamerule checkpointOnWorldCreation true`
*   **Description:** Automatically sets a checkpoint when a new world is created.

### Random Anchor Player

*   **Usage:** `/gamerule randomAnchorEnabled false`
*   **Description:** Randomly picks a new anchor player each time an automatic checkpoint is set.

### Any Player Death Triggers Restore
*   **Usage:** `/gamerule anyPlayerDeathTriggersRestore false`
*   **Description:** Whether the checkpoint is restored if any player dies (true) or only when the anchor player dies (false).

### Checkpoint When All Players Sleep
*   **Usage:** `/gamerule checkpointOnSleep false`
*   **Description:** Sets a checkpoint when all players sleep and the night is skipped.
