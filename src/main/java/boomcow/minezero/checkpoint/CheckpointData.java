package boomcow.minezero.checkpoint;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.*;

public class CheckpointData extends SavedData {
    public static final String DATA_NAME = "global_checkpoint";
    private static final Logger LOGGER = LogUtils.getLogger(); 

    // NBT Keys
    private static final String KEY_CHECKPOINT_DIMENSION = "checkpointDimension";
    private static final String KEY_PLAYERS_DATA = "playersData";
    private static final String KEY_FIRE_TICKS = "fireTicks";
    private static final String KEY_ANCHOR_PLAYER_UUID = "anchorPlayerUUID";
    private static final String KEY_CHECKPOINT_POS_X = "checkpointPosX";
    private static final String KEY_CHECKPOINT_POS_Y = "checkpointPosY";
    private static final String KEY_CHECKPOINT_POS_Z = "checkpointPosZ";
    private static final String KEY_CHECKPOINT_HEALTH = "checkpointHealth";
    private static final String KEY_CHECKPOINT_HUNGER = "checkpointHunger";
    private static final String KEY_CHECKPOINT_XP = "checkpointXP";
    private static final String KEY_CHECKPOINT_DAY_TIME = "checkpointDayTime";
    private static final String KEY_ENTITY_DATA = "entityData";
    private static final String KEY_ENTITY_DIMENSIONS = "entityDimensions";
    private static final String KEY_ENTITY_AGGRO_TARGETS = "entityAggroTargets";
    private static final String KEY_GROUND_ITEMS = "groundItems";
    private static final String KEY_CHECKPOINT_INVENTORY = "checkpointInventory";
    private static final String KEY_WORLD_DATA = "worldCheckpointData";

    private ResourceKey<Level> checkpointDimension;
    public void setCheckpointDimension(ResourceKey<Level> dimension) {
        this.checkpointDimension = dimension;
        this.setDirty();
    }



    public ResourceKey<Level> getCheckpointDimension() {
        return checkpointDimension;
    }
    private Map<UUID, CompoundTag> playersData = new HashMap<>();

    public void savePlayerData(UUID uuid, PlayerData pdata) {
        playersData.put(uuid, pdata.toNBT());
        setDirty();
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (playersData.containsKey(uuid)) {
            return PlayerData.fromNBT(playersData.get(uuid));
        }
        return null;
    }


    private int fireTicks;

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
        this.setDirty();
    }

    public int getFireTicks() {
        return fireTicks;
    }

    private UUID anchorPlayerUUID;

    public void setAnchorPlayerUUID(UUID anchorPlayerUUID) {
        this.anchorPlayerUUID = anchorPlayerUUID;
        this.setDirty();
    }

    public UUID getAnchorPlayerUUID() {
        return anchorPlayerUUID;
    }


    private BlockPos checkpointPos;
    private List<ItemStack> checkpointInventory = new ArrayList<>();
    private float checkpointHealth;
    private int checkpointHunger;
    private int checkpointXP;

    private long checkpointDayTime;
    private List<CompoundTag> entityData = new ArrayList<>();
    private List<ResourceKey<Level>> entityDimensions = new ArrayList<>();
    private Map<UUID, UUID> entityAggroTargets = new HashMap<>();



    private List<CompoundTag> groundItems = new ArrayList<>();

    private WorldData worldData = new WorldData();

    public CheckpointData() {
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public void saveWorldData(ServerLevel level) {
        this.worldData.clearWorldData();
        this.worldData.saveAllLoadedChunks(level);
        this.worldData.saveWeather(level);
        this.worldData.saveDayTime(level.getDayTime());
        this.worldData.saveGameTime(level.getGameTime());
        this.worldData.saveCheckpointTick(level.getGameTime());
        this.setDirty();
    }

    public static CheckpointData load(CompoundTag nbt) {
        CheckpointData data = new CheckpointData(); 

        if (nbt.contains(KEY_CHECKPOINT_DIMENSION, Tag.TAG_STRING)) {
            data.checkpointDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString(KEY_CHECKPOINT_DIMENSION)));
        }

        if (nbt.contains(KEY_PLAYERS_DATA, Tag.TAG_COMPOUND)) {
            CompoundTag playersTag = nbt.getCompound(KEY_PLAYERS_DATA);
            for (String key : playersTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    data.playersData.put(uuid, playersTag.getCompound(key));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to parse UUID from key '{}' in PlayersData NBT", key, e);
                }
            }
        }

        data.fireTicks = nbt.getInt(KEY_FIRE_TICKS);

        if (nbt.hasUUID(KEY_ANCHOR_PLAYER_UUID)) {
            data.anchorPlayerUUID = nbt.getUUID(KEY_ANCHOR_PLAYER_UUID);
        }

        if (nbt.contains(KEY_CHECKPOINT_POS_X) && nbt.contains(KEY_CHECKPOINT_POS_Y) && nbt.contains(KEY_CHECKPOINT_POS_Z)) {
            data.checkpointPos = new BlockPos(nbt.getInt(KEY_CHECKPOINT_POS_X), nbt.getInt(KEY_CHECKPOINT_POS_Y), nbt.getInt(KEY_CHECKPOINT_POS_Z));
        }
        data.checkpointHealth = nbt.getFloat(KEY_CHECKPOINT_HEALTH);
        data.checkpointHunger = nbt.getInt(KEY_CHECKPOINT_HUNGER);
        data.checkpointXP = nbt.getInt(KEY_CHECKPOINT_XP);
        data.checkpointDayTime = nbt.getLong(KEY_CHECKPOINT_DAY_TIME);

        if (nbt.contains(KEY_CHECKPOINT_INVENTORY, Tag.TAG_LIST)) {
            ListTag invListTag = nbt.getList(KEY_CHECKPOINT_INVENTORY, Tag.TAG_COMPOUND);
            data.checkpointInventory.clear();
            for (int i = 0; i < invListTag.size(); i++) {
                data.checkpointInventory.add(ItemStack.of(invListTag.getCompound(i)));
            }
        }

        if (nbt.contains(KEY_ENTITY_DATA, Tag.TAG_LIST)) {
            ListTag entityListTag = nbt.getList(KEY_ENTITY_DATA, Tag.TAG_COMPOUND);
            data.entityData.clear();
            for (int i = 0; i < entityListTag.size(); i++) {
                data.entityData.add(entityListTag.getCompound(i));
            }
        }

        if (nbt.contains(KEY_ENTITY_DIMENSIONS, Tag.TAG_LIST)) {
            ListTag entityDimListTag = nbt.getList(KEY_ENTITY_DIMENSIONS, Tag.TAG_STRING);
            data.entityDimensions.clear();
            for (int i = 0; i < entityDimListTag.size(); i++) {
                try {
                    data.entityDimensions.add(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(entityDimListTag.getString(i))));
                } catch (Exception e) {
                    LOGGER.warn("Failed to load entity dimension from NBT: {}", entityDimListTag.getString(i), e);
                }
            }
        }
        
        if (data.entityData.size() > 0 && data.entityDimensions.size() > 0 && data.entityData.size() != data.entityDimensions.size()) {
            LOGGER.warn("Mismatch in loaded entityData ({}) and entityDimensions ({}). Checkpoint entity data might be incomplete.", data.entityData.size(), data.entityDimensions.size());
            
        }


        if (nbt.contains(KEY_ENTITY_AGGRO_TARGETS, Tag.TAG_COMPOUND)) {
            CompoundTag aggroTag = nbt.getCompound(KEY_ENTITY_AGGRO_TARGETS);
            data.entityAggroTargets.clear();
            for (String key : aggroTag.getAllKeys()) {
                try {
                    UUID entityUUID = UUID.fromString(key);
                    if (aggroTag.hasUUID(key)) { 
                        UUID targetUUID = aggroTag.getUUID(key);
                        data.entityAggroTargets.put(entityUUID, targetUUID);
                    } else {
                        LOGGER.warn("Aggro target for {} is not a valid UUID in NBT.", key);
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to parse UUID for aggro target key '{}' from NBT", key, e);
                }
            }
        }

        if (nbt.contains(KEY_GROUND_ITEMS, Tag.TAG_LIST)) {
            ListTag groundItemsListTag = nbt.getList(KEY_GROUND_ITEMS, Tag.TAG_COMPOUND);
            data.groundItems.clear();
            for (int i = 0; i < groundItemsListTag.size(); i++) {
                data.groundItems.add(groundItemsListTag.getCompound(i));
            }
        }

        
        if (nbt.contains(KEY_WORLD_DATA, Tag.TAG_COMPOUND)) {
            data.worldData.loadFromNBT(nbt.getCompound(KEY_WORLD_DATA));
        } else {
            LOGGER.warn("Checkpoint NBT is missing WorldData. A new empty WorldData will be used.");
            
        }
        LOGGER.debug("CheckpointData loaded from NBT.");
        return data;
    }

    
    @Override
    public CompoundTag save(CompoundTag nbt) {
        if (checkpointDimension != null) {
            nbt.putString(KEY_CHECKPOINT_DIMENSION, checkpointDimension.location().toString());
        }

        CompoundTag playersNbt = new CompoundTag();
        for (Map.Entry<UUID, CompoundTag> entry : playersData.entrySet()) {
            playersNbt.put(entry.getKey().toString(), entry.getValue());
        }
        nbt.put(KEY_PLAYERS_DATA, playersNbt);

        nbt.putInt(KEY_FIRE_TICKS, fireTicks);

        if (anchorPlayerUUID != null) {
            nbt.putUUID(KEY_ANCHOR_PLAYER_UUID, anchorPlayerUUID);
        }

        if (checkpointPos != null) {
            nbt.putInt(KEY_CHECKPOINT_POS_X, checkpointPos.getX());
            nbt.putInt(KEY_CHECKPOINT_POS_Y, checkpointPos.getY());
            nbt.putInt(KEY_CHECKPOINT_POS_Z, checkpointPos.getZ());
        }
        nbt.putFloat(KEY_CHECKPOINT_HEALTH, checkpointHealth);
        nbt.putInt(KEY_CHECKPOINT_HUNGER, checkpointHunger);
        nbt.putInt(KEY_CHECKPOINT_XP, checkpointXP);
        nbt.putLong(KEY_CHECKPOINT_DAY_TIME, checkpointDayTime);

        ListTag invListTag = new ListTag();
        for (ItemStack stack : checkpointInventory) {
            if (!stack.isEmpty()) { 
                invListTag.add(stack.save(new CompoundTag()));
            }
        }
        nbt.put(KEY_CHECKPOINT_INVENTORY, invListTag);

        ListTag entityListNbt = new ListTag();
        for (CompoundTag entityNBT : entityData) {
            entityListNbt.add(entityNBT);
        }
        nbt.put(KEY_ENTITY_DATA, entityListNbt);

        ListTag entityDimListNbt = new ListTag();
        for (ResourceKey<Level> dimKey : entityDimensions) {
            if (dimKey != null) { 
                entityDimListNbt.add(StringTag.valueOf(dimKey.location().toString()));
            }
        }
        nbt.put(KEY_ENTITY_DIMENSIONS, entityDimListNbt);

        CompoundTag aggroTargetsNbt = new CompoundTag();
        for (Map.Entry<UUID, UUID> entry : entityAggroTargets.entrySet()) {
            aggroTargetsNbt.putUUID(entry.getKey().toString(), entry.getValue());
        }
        nbt.put(KEY_ENTITY_AGGRO_TARGETS, aggroTargetsNbt);

        ListTag groundItemsListNbt = new ListTag();
        for (CompoundTag itemNBT : groundItems) {
            groundItemsListNbt.add(itemNBT);
        }
        nbt.put(KEY_GROUND_ITEMS, groundItemsListNbt);

        CompoundTag worldDataNbt = new CompoundTag();
        if (this.worldData != null) { 
            this.worldData.saveToNBT(worldDataNbt);
        }
        nbt.put(KEY_WORLD_DATA, worldDataNbt);
        LOGGER.debug("CheckpointData saved to NBT.");
        return nbt; 
    }

    public static CheckpointData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(CheckpointData::load, CheckpointData::new, CheckpointData.DATA_NAME);
    }

    public String toString(ServerLevel level) {
        CheckpointData data = get(level); 
        StringBuilder sb = new StringBuilder();
        sb.append("CheckpointData:\n");
        sb.append("AnchorPlayerUUID: ").append(data.getAnchorPlayerUUID()).append("\n");
        sb.append("CheckpointPos: ").append(data.getCheckpointPos()).append("\n");
        sb.append("CheckpointHealth: ").append(data.getCheckpointHealth()).append("\n");
        sb.append("CheckpointHunger: ").append(data.getCheckpointHunger()).append("\n");
        sb.append("CheckpointXP: ").append(data.getCheckpointXP()).append("\n");
        sb.append("CheckpointDayTime: ").append(data.getCheckpointDayTime()).append("\n");
        sb.append("FireTicks: ").append(data.getFireTicks()).append("\n");
        sb.append("EntityData: ").append(data.getEntityData()).append("\n");
        sb.append("GroundItems: ").append(data.getGroundItems()).append("\n");
        sb.append("CheckpointInventory: ").append(data.getCheckpointInventory()).append("\n");
        sb.append("PlayersData: ").append(data.playersData).append("\n");
        return sb.toString();
    }


    public void setCheckpointPos(BlockPos pos) {
        this.checkpointPos = pos;
        this.setDirty();
    }

    public void setCheckpointHealth(float health) {
        this.checkpointHealth = health;
        this.setDirty();
    }

    public void setEntityAggroTargets(Map<UUID, UUID> aggroTargets) {
        this.entityAggroTargets = aggroTargets;
        this.setDirty();
    }

    public Map<UUID, UUID> getEntityAggroTargets() {
        return entityAggroTargets;
    }


    public void setCheckpointInventory(List<ItemStack> inv) {
        this.checkpointInventory = inv;
        this.setDirty();
    }

    public void setCheckpointHunger(int hunger) {
        this.checkpointHunger = hunger;
        this.setDirty();
    }

    public void setCheckpointXP(int xp) {
        this.checkpointXP = xp;
        this.setDirty();
    }

    public void setCheckpointDayTime(long dayTime) {
        this.checkpointDayTime = dayTime;
        this.setDirty();
    }

    public void setEntityData(List<CompoundTag> entities) {
        this.entityData = entities;
        this.setDirty();
    }

    public void setGroundItems(List<CompoundTag> groundItems) {
        this.groundItems = groundItems;
        this.setDirty();
    }

    public BlockPos getCheckpointPos() {
        return checkpointPos;
    }

    public float getCheckpointHealth() {
        return checkpointHealth;
    }

    public int getCheckpointHunger() {
        return checkpointHunger;
    }

    public int getCheckpointXP() {
        return checkpointXP;
    }

    public long getCheckpointDayTime() {
        return checkpointDayTime;
    }

    public List<ItemStack> getCheckpointInventory() {
        return checkpointInventory;
    }

    public List<CompoundTag> getEntityData() {
        return entityData;
    }
    public List<ResourceKey<Level>> getEntityDimensions() {
        return entityDimensions;
    }
    public void setEntityDataWithDimensions(List<CompoundTag> entities, List<ResourceKey<Level>> dimensions) {
        this.entityData = entities;
        this.entityDimensions = dimensions;
        this.setDirty();
    }

    public List<CompoundTag> getGroundItems() {
        return groundItems;
    }

    @Override
    public String toString() { 
        StringBuilder sb = new StringBuilder();
        sb.append("CheckpointData Instance:\n");
        sb.append("  AnchorPlayerUUID: ").append(anchorPlayerUUID).append("\n");
        sb.append("  CheckpointPos: ").append(checkpointPos).append("\n");
        sb.append("  CheckpointDimension: ").append(checkpointDimension != null ? checkpointDimension.location() : "null").append("\n");
        sb.append("  CheckpointHealth: ").append(checkpointHealth).append("\n");
        sb.append("  CheckpointHunger: ").append(checkpointHunger).append("\n");
        sb.append("  CheckpointXP: ").append(checkpointXP).append("\n");
        sb.append("  CheckpointDayTime: ").append(checkpointDayTime).append("\n");
        sb.append("  FireTicks: ").append(fireTicks).append("\n");
        sb.append("  EntityData Count: ").append(entityData.size()).append("\n");
        sb.append("  EntityDimensions Count: ").append(entityDimensions.size()).append("\n");
        sb.append("  GroundItems Count: ").append(groundItems.size()).append("\n");
        sb.append("  CheckpointInventory Count: ").append(checkpointInventory.size()).append("\n");
        sb.append("  PlayersData Count: ").append(playersData.size()).append("\n");
        sb.append("  WorldData Present: ").append(worldData != null).append("\n");
        return sb.toString();
    }


}

