## KNOWN ISSUES

- **Issue 2**: If you are on fire when you die, after the restore you will still be on fire.
  - **Status**: Open/In Progress/Resolved
  - **Reproduction Steps**:
    1. Set a checkpoint using `/setcheckpoint`.
    2. Set yourself on fire (e.g., using a Flint and Steel).
    3. Die (e.g., jump from a height or `/kill`).
    4. **Expected Result**: Player respawns at the checkpoint without being on fire.
    5. **Actual Result**: Player respawns at the checkpoint but remains on fire.
  - **Workaround**: Extinguish yourself before dying.
- **Issue 3**: Dying in the nether does not reset you until you go back to nether
  -  **Status**: Open
  - **Reproduction Steps**:
    1. Set a checkpoint in the Overworld using `/setcheckpoint`.
    2. Enter the Nether.
    3. Die in the Nether (e.g., jump from a height or `/kill`).
    4. **Expected Result**: Player respawns at the Overworld checkpoint.
    5. **Actual Result**: Player respawns in the overworld, but cannot mine and does not get inventory back until going to the nether.
  - **Workaround**: Return to the Nether to fix.
- **Issue 3**: Breaking block where water flows into block does not restore original block
  -  **Status**: Open
  - **Reproduction Steps**:
    1. Set a checkpoint using `/setcheckpoint`.
    2. Place a water source block next to another block (e.g., Dirt).
    3. Break the Dirt block.
    4. Trigger a restore (e.g., die or use `/restorecheckpoint`).
    5. **Expected Result**: The broken Dirt block should be restored.
    6. **Actual Result**: The broken Dirt block is not restored; water remains.
  - **Workaround**: Avoid breaking blocks adjacent to flowing water before triggering a restore.
