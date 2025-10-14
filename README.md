# MineZero

**MineZero** is a Minecraft mod inspired by *Re:Zero* that introduces a **checkpoint and world reset system**. When the designated **anchor player** dies, the entire world (including players, entities, and blocks) is restored to the last checkpoint — effectively letting you "return by death."

---



## CurseForge

Get releases on CurseForge: [MineZero — Return By Death](https://www.curseforge.com/minecraft/mc-mods/mine-zero-return-by-death)

---

## Features

* **Checkpoints**

  * Set manually with the `/setcheckpoint` command (OP level 2).
  * Use the **Artifact Flute** item to set a checkpoint instantly.
  * Saves player data, inventory, health, hunger, XP, potion effects, advancements, and dimension.
  * Saves world data including block states, entities, items on the ground, and time of day.

* **Anchor Player System**

  * Only the **anchor player’s death** triggers a reset.
    * This can be changed via the gamerule **"enableAllPlayersRBD"**
    * Alternatively you can let the anchor be randomly chosen on each new checkpoint with **"enableRandomAnchor"**
  * The anchor player is set automatically when they create a checkpoint.

* **World Reset on Death**

  * Restores players to their saved states.
  * Resets world blocks, entities, and items.
  * Rewinds daytime.
  * Plays a custom **death chime** sound.

---

## Branches

This repository maintains multiple branches for different versions and mod loaders:

* `main` – Project root and documentation.
* `1.20.1-forge` – Forge mod loader, Minecraft 1.20.1.
* `1.21.1-neoforge` – NeoForge mod loader, Minecraft 1.21.1.
* `1.20.1-fabric` – Fabric mod loader, Minecraft 1.20.1.
* `1.12.2-forge` – Legacy Forge support, Minecraft 1.12.2.

---

## Version Status

| Branch           | Loader    | MC Version | Status     |
|------------------|-----------|------------|------------|
| 1.20.1-forge     | Forge     | 1.20.1     | Released   |
| 1.21.1-neoforge  | NeoForge  | 1.21.1     | Released   |
| 1.21.1-fabric    | Fabric    | 1.21.1     | In Progress |
| 1.12.2-forge     | Forge     | 1.12.2     | In Progress |

## Commands

* `/setcheckpoint` → Sets the checkpoint for the executing player (requires OP level 2).

---

## Items

* **Artifact Flute** → A special item that instantly sets a checkpoint when used.

---

## Sound Events

* **death_chime** → Plays when the anchor player dies and the world resets.

---

## Getting Started

1. Clone the repository.
2. Switch to the branch that matches your desired version and loader.
3. Import into your modding environment (Forge/NeoForge/Fabric).
4. Build with Gradle.

---

## Contributing

Contributions are welcome! Please:

* Fork the repo
* Create a feature branch (`git checkout -b feature/new-thing`)
* Submit a pull request

---

## License

This project is licensed under the [Apache License 2.0](LICENSE).

---

## Credits

* Inspired by *Re:Zero − Starting Life in Another World*.
* Developed by **Austin Perez**.

---

## Future Plans

* Expand compatibility across versions.

---

[![Join Discord](https://i.imgur.com/2F7fEU8.gif)](https://discord.gg/S9zrhP8e5B)

## Join My Discord for Feedback & Suggestions!

If you're enjoying my mod and want to help shape its future, I'd love to hear from you! Join my [Discord server](https://discord.gg/S9zrhP8e5B) to share feedback, report bugs, suggest new features, or discuss Minecraft modding. Your input is valuable and will directly influence the mod's development.

---

## Documentation

- How it works: [docs/HowItWorks.md](docs/HowItWorks.md)

