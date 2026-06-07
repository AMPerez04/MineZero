package boomcow.minezero.checkpoint;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
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
    public float saturation;
    public float exhaustion;
    public int airSupply;
    public int experienceLevel;
    public float experienceProgress;
    public int fireTicks;
    public int selectedHotbarSlot;
    public ResourceKey<Level> dimension;
    public List<ItemStack> inventory = new ArrayList<>();
    public String gameMode;
    public double spawnX;
    public double spawnY;
    public double spawnZ;
    public ResourceKey<Level> spawnDimension;
    public boolean spawnForced;

    public List<MobEffectInstance> potionEffects = new ArrayList<>();

    public CompoundTag advancements = new CompoundTag();

    public CompoundTag toNBT() {
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
        tag.putFloat("Saturation", saturation);
        tag.putFloat("Exhaustion", exhaustion);
        tag.putInt("AirSupply", airSupply);

        tag.putInt("FireTicks", fireTicks);
        tag.putInt("SelectedHotbarSlot", selectedHotbarSlot);
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
            CompoundTag effectTag = new CompoundTag();
            effect.save(effectTag);
            effectsTag.add(effectTag);
        }
        tag.put("PotionEffects", effectsTag);

        if (advancements != null) {
            tag.put("Advancements", advancements);
        }

        if (dimension != null) {
            tag.putString("Dimension", dimension.location().toString());
        }

        CompoundTag invTag = new CompoundTag();
        for (int i = 0; i < inventory.size(); i++) {
            CompoundTag stackTag = new CompoundTag();
            inventory.get(i).save(stackTag);
            invTag.put("Slot" + i, stackTag);
        }
        tag.put("Inventory", invTag);

        return tag;
    }

    public static PlayerData fromNBT(CompoundTag tag) {
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
        data.saturation = tag.getFloat("Saturation");
        data.exhaustion = tag.getFloat("Exhaustion");
        data.airSupply = tag.getInt("AirSupply");
        data.fireTicks = tag.getInt("FireTicks");
        data.selectedHotbarSlot = tag.contains("SelectedHotbarSlot") ? tag.getInt("SelectedHotbarSlot") : 0;

        data.spawnX = tag.getDouble("SpawnX");
        data.spawnY = tag.getDouble("SpawnY");
        data.spawnZ = tag.getDouble("SpawnZ");
        data.spawnForced = tag.getBoolean("SpawnForced");

        data.experienceLevel = tag.getInt("ExperienceLevel");
        data.experienceProgress = tag.getFloat("ExperienceProgress");

        if (tag.contains("SpawnDimension")) {
            data.spawnDimension = ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(tag.getString("SpawnDimension")));
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

        if (tag.contains("Dimension")) {
            data.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("Dimension")));
        }

        if (tag.contains("GameMode")) {
            data.gameMode = tag.getString("GameMode");
        }

        CompoundTag invTag = tag.getCompound("Inventory");
        data.inventory.clear();
        int i = 0;
        while (invTag.contains("Slot" + i)) {
            CompoundTag stackTag = invTag.getCompound("Slot" + i);
            data.inventory.add(ItemStack.of(stackTag));
            i++;
        }

        return data;
    }
}
