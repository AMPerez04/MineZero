package boomcow.minezero.checkpoint;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    public double posX;
    public double posY;
    public double posZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    public float fallDistance;
    public float yaw;
    public float pitch;
    public float health;
    public int hunger;
    public int experienceLevel;
    public float experienceProgress;
    public int fireTicks;
    public int airSupply;
    public ResourceKey<Level> dimension;
    public String gameMode;
    public double spawnX;
    public double spawnY;
    public double spawnZ;
    public ResourceKey<Level> spawnDimension;
    public boolean spawnForced;
    public List<MobEffectInstance> potionEffects = new ArrayList<>();
    public CompoundTag advancements = new CompoundTag();

    public ListTag inventoryNBT = new ListTag();

    public CompoundTag toNBT(HolderLookup.Provider lookupProvider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("PosX", posX);
        tag.putDouble("PosY", posY);
        tag.putDouble("PosZ", posZ);
        tag.putDouble("MotionX", motionX);
        tag.putDouble("MotionY", motionY);
        tag.putDouble("MotionZ", motionZ);
        tag.putFloat("FallDistance", fallDistance);
        tag.putFloat("Yaw", yaw);
        tag.putFloat("Pitch", pitch);
        tag.putFloat("Health", health);
        tag.putInt("Hunger", hunger);
        tag.putInt("FireTicks", fireTicks);
        tag.putInt("AirSupply", airSupply);
        tag.putString("GameMode", gameMode);
        tag.putInt("ExperienceLevel", experienceLevel);
        tag.putFloat("ExperienceProgress", experienceProgress);
        tag.putDouble("SpawnX", spawnX);
        tag.putDouble("SpawnY", spawnY);
        tag.putDouble("SpawnZ", spawnZ);
        tag.putBoolean("SpawnForced", spawnForced);

        if (spawnDimension != null) {
            tag.putString("SpawnDimension", spawnDimension.location().toString());
        }
        ListTag effectsTag = new ListTag();
        for (MobEffectInstance effect : potionEffects) {
            Tag effectNbtTag = effect.save();
            effectsTag.add(effectNbtTag);
        }
        tag.put("PotionEffects", effectsTag);
        if (advancements != null) {
            tag.put("Advancements", advancements);
        }
        if (dimension != null) {
            tag.putString("Dimension", dimension.location().toString());
        }

        tag.put("Inventory", this.inventoryNBT);



        return tag;
    }

    public static PlayerData fromNBT(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        PlayerData data = new PlayerData();
        data.posX = tag.getDouble("PosX");
        data.posY = tag.getDouble("PosY");
        data.posZ = tag.getDouble("PosZ");
        data.fallDistance = tag.getFloat("FallDistance");
        data.motionX = tag.getDouble("MotionX");
        data.motionY = tag.getDouble("MotionY");
        data.motionZ = tag.getDouble("MotionZ");
        data.yaw = tag.getFloat("Yaw");
        data.pitch = tag.getFloat("Pitch");
        data.health = tag.getFloat("Health");
        data.hunger = tag.getInt("Hunger");
        data.fireTicks = tag.getInt("FireTicks");
        data.airSupply = tag.getInt("AirSupply");
        data.spawnX = tag.getDouble("SpawnX");
        data.spawnY = tag.getDouble("SpawnY");
        data.spawnZ = tag.getDouble("SpawnZ");
        data.spawnForced = tag.getBoolean("SpawnForced");
        data.experienceLevel = tag.getInt("ExperienceLevel");
        data.experienceProgress = tag.getFloat("ExperienceProgress");


        if (tag.contains("SpawnDimension", Tag.TAG_STRING)) {
            String spawnDimString = tag.getString("SpawnDimension");
            try {
                data.spawnDimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(spawnDimString));
            } catch (Exception e) {
                System.err.println("Failed to parse SpawnDimension ResourceLocation string: " + spawnDimString + " - " + e.getMessage());
                data.spawnDimension = null;
            }
        }
        data.potionEffects.clear();
        ListTag effectsTag = tag.getList("PotionEffects", 10);
        for (int i = 0; i < effectsTag.size(); i++) {
            CompoundTag effectTag = effectsTag.getCompound(i);
            MobEffectInstance effect = MobEffectInstance.load(effectTag);
            if (effect != null) {
                data.potionEffects.add(effect);
            }
        }
        if (tag.contains("Advancements")) {
            data.advancements = tag.getCompound("Advancements");
        }
        if (tag.contains("Dimension", Tag.TAG_STRING)) {
            String currentDimString = tag.getString("Dimension");
            try {
                data.dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(currentDimString));
            } catch (Exception e) {
                System.err.println("Failed to parse player Dimension ResourceLocation string: " + currentDimString + " - " + e.getMessage());
                data.dimension = null;
            }
        }

        if (tag.contains("GameMode")) {
            data.gameMode = tag.getString("GameMode");
        }
        if (tag.contains("Inventory", Tag.TAG_LIST)) {
            data.inventoryNBT = tag.getList("Inventory", Tag.TAG_COMPOUND);
        }


        return data;
    }
}
