# MineZero Testing Guide

## Prerequisite Setup
1. **Permissions**: Ensure you are an operator (`/op <yourname>`).
2. **Designate Anchor**: Run `/setanchor <yourname>` to make yourself the anchor player whose death triggers the world restore.
3. **Game Rules**: Ensure `keepInventory` is `false` (default) to properly test the mod's inventory restoration overriding vanilla death.

## Test Cases

### 1. The Basic Loop (Sanity Check)
- [ ] **Action**: Run `/setcheckpoint`.
- [ ] **Action**: Eat food, take damage, drop an item from your inventory.
- [ ] **Action**: Die (e.g., jump from a height or `/kill`).
- [ ] **Result**:
    - You should respawn at the exact checkpoint location/rotation.
    - Health and Hunger should be exactly as they were.
    - Inventory should match the checkpoint state (dropped item returns).

### 2. World Manipulation (Block Tracking)
*The mod manually tracks block breaks and places. This is the most fragile part.*
- [ ] **Action**: Run `/setcheckpoint`.
- [ ] **Action**: Place a block (e.g., Cobblestone).
- [ ] **Action**: Break an existing block (e.g., Dirt).
- [ ] **Action**: Trigger a restore (`/restorecheckpoint` or die).
- [ ] **Result**:
    - Placed Cobblestone should disappear (become Air).
    - Broken Dirt should reappear.

### 3. Container & Tile Entity State
- [ ] **Action**: Place a Chest. Put 1 Diamond inside.
- [ ] **Action**: Run `/setcheckpoint`.
- [ ] **Action**: Open the chest, take the Diamond, and put in Dirt.
- [ ] **Action**: Trigger a restore.
- [ ] **Result**:
    - Chest should contain the Diamond.
    - Chest should NOT contain the Dirt.
    - Your inventory should NOT contain the Diamond.

### 4. Entity & Mob State
- [ ] **Action**: Spawn a Zombie and a Cow nearby.
- [ ] **Action**: Run `/setcheckpoint`.
- [ ] **Action**: Kill the Cow.
- [ ] **Action**: Spawn a new Pig.
- [ ] **Action**: Trigger a restore.
- [ ] **Result**:
    - The Cow should be alive again.
    - The Zombie should be in its approximate original position/health.
    - The new Pig should disappear.

### 5. Environmental State
- [ ] **Action**: Set time to Day (`/time set day`). Clear weather.
- [ ] **Action**: Run `/setcheckpoint`.
- [ ] **Action**: Set time to Night (`/time set night`). Start rain (`/weather rain`).
- [ ] **Action**: Trigger a restore.
- [ ] **Result**:
    - Time should revert to Day.
    - Rain should stop immediately.

### 6. Dimensional Travel & Persistence
- [ ] **Action**: Run `/setcheckpoint` in the Overworld.
- [ ] **Action**: Enter a Nether Portal.
- [ ] **Action**: Disconnect and **Close Minecraft completely**.
- [ ] **Action**: Relaunch and Join the world (you should spawn in the Nether).
- [ ] **Action**: Trigger a restore (`/restorecheckpoint`).
- [ ] **Result**:
    - You should load back into the Overworld at the checkpoint.
    - The checkpoint data should not be lost.

## Debug Commands
- `/restorecheckpoint`: Manually triggers the restoration without needing to die. Useful for quick iteration.
- `/setcheckpoint`: Forces a new save state at your current position.

## Known Limitations / Edge Cases to Watch
- **Modded Blocks**: Blocks from other mods that have complex internal states (machines, pipes) might not save/restore correctly if they don't rely on standard NBT or BlockState.
- **Chunk Loading**: If you travel very far (unloading the checkpoint chunk) and die, verify that the game doesn't crash or lag excessively upon reloading those chunks.
