package boomcow.minezero.checkpoint;

import boomcow.minezero.util.LightningScheduler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CheckpointManager {


    public static void setCheckpoint(ServerPlayer anchorPlayer) {
        Logger logger = LogManager.getLogger();
        logger.info("Setting checkpoint...");
        long startTime = System.nanoTime();
        ServerLevel level = anchorPlayer.serverLevel();
        CheckpointData data = CheckpointData.get(level);
        HolderLookup.Provider lookupProvider = level.registryAccess();
        data.saveWorldData(level);
        data.setAnchorPlayerUUID(anchorPlayer.getUUID());
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            PlayerData pdataObject = new PlayerData();
            pdataObject.posX = player.getX();
            pdataObject.posY = player.getY();
            pdataObject.posZ = player.getZ();
            pdataObject.yaw = player.getYRot();
            pdataObject.pitch = player.getXRot();
            pdataObject.dimension = player.level().dimension();
            GameType type = player.gameMode.getGameModeForPlayer();
            pdataObject.gameMode = type.getName().toLowerCase(Locale.ROOT);

            pdataObject.motionX = player.getDeltaMovement().x;
            pdataObject.motionY = player.getDeltaMovement().y;
            pdataObject.motionZ = player.getDeltaMovement().z;
            pdataObject.fallDistance = player.fallDistance;

            pdataObject.health = player.getHealth();
            pdataObject.hunger = player.getFoodData().getFoodLevel();
            pdataObject.experienceLevel = player.experienceLevel;
            pdataObject.experienceProgress = player.experienceProgress;
            pdataObject.fireTicks = player.getRemainingFireTicks();
            pdataObject.airSupply = player.getAirSupply();

            BlockPos spawn = player.getRespawnPosition();
            ResourceKey<Level> spawnDim = player.getRespawnDimension();
            if (spawn != null && spawnDim != null) {
                pdataObject.spawnX = spawn.getX() + 0.5;
                pdataObject.spawnY = spawn.getY();
                pdataObject.spawnZ = spawn.getZ() + 0.5;
                pdataObject.spawnDimension = spawnDim;
                pdataObject.spawnForced = player.isRespawnForced();
            }

            pdataObject.potionEffects.clear();
            for (MobEffectInstance effect : player.getActiveEffects()) {
                pdataObject.potionEffects.add(new MobEffectInstance(effect));
            }

            Inventory playerInventory = player.getInventory();
            ListTag inventoryTag = new ListTag();
            for (int i = 0; i < playerInventory.items.size(); ++i) {
                ItemStack stack = playerInventory.items.get(i);
                if (!stack.isEmpty()) {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putByte("Slot", (byte) i);
                    Tag savedItemTag = stack.save(player.registryAccess(), compoundtag);
                    inventoryTag.add(savedItemTag);
                }
            }
            for (int j = 0; j < playerInventory.armor.size(); ++j) {
                ItemStack stack = playerInventory.armor.get(j);
                if (!stack.isEmpty()) {
                    CompoundTag compoundtag1 = new CompoundTag();
                    compoundtag1.putByte("Slot", (byte) (j + 100));
                    Tag savedItemTag = stack.save(player.registryAccess(), compoundtag1);
                    inventoryTag.add(savedItemTag);
                }
            }
            for (int k = 0; k < playerInventory.offhand.size(); ++k) {
                ItemStack stack = playerInventory.offhand.get(k);
                if (!stack.isEmpty()) {
                    CompoundTag compoundtag2 = new CompoundTag();
                    compoundtag2.putByte("Slot", (byte) (k + 150));
                    Tag savedItemTag = stack.save(player.registryAccess(), compoundtag2);
                    inventoryTag.add(savedItemTag);
                }
            }

            pdataObject.inventoryNBT = inventoryTag;

            logger.info("Saving inventory for player {}: Generated ListTag with {} entries. NBT: {}",
                    player.getName().getString(), inventoryTag.size(), inventoryTag.toString());

            CompoundTag capturedAdvancementsNBT = new CompoundTag();
            if (player.server != null) {
                for (AdvancementHolder advancementHolder : player.server.getAdvancements().getAllAdvancements()) {
                    AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementHolder);
                    CompoundTag individualAdvancementProgressTag = new CompoundTag();
                    if (progress != null) {
                        for (String criterionName : progress.getCompletedCriteria()) {
                            individualAdvancementProgressTag.putBoolean(criterionName, true);
                        }
                    }
                    if (!individualAdvancementProgressTag.isEmpty()) {
                        capturedAdvancementsNBT.put(advancementHolder.id().toString(), individualAdvancementProgressTag);
                    }
                }
            }
            pdataObject.advancements = capturedAdvancementsNBT;
            CompoundTag playerDataNbt = pdataObject.toNBT(lookupProvider);
            data.savePlayerData(player.getUUID(), playerDataNbt);
        }
        data.setCheckpointDayTime(level.getDayTime());
        List<CompoundTag> entityList = new ArrayList<>();
        List<ResourceKey<Level>> entityDimensions = new ArrayList<>();
        Map<UUID, UUID> entityAggroTargets = new HashMap<>();
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof Mob mob) {
                    CompoundTag entityNBT = new CompoundTag();
                    entity.save(entityNBT);

                    if (EntityType.byString(entityNBT.getString("id")).isPresent()) {
                        entityList.add(entityNBT);
                        entityDimensions.add(serverLevel.dimension());
                        if (mob.getTarget() != null) {
                            entityAggroTargets.put(mob.getUUID(), mob.getTarget().getUUID());
                        }
                    }
                }
            }
        }
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof Mob) continue;

                if (entity instanceof AbstractMinecart ||
                        entity instanceof AreaEffectCloud ||
                        entity instanceof Boat ||
                        entity instanceof EndCrystal ||
                        entity instanceof EvokerFangs ||
                        entity instanceof ExperienceOrb ||
                        entity instanceof EyeOfEnder ||
                        entity instanceof FallingBlockEntity ||
                        entity instanceof HangingEntity ||
                        entity instanceof ItemEntity ||
                        entity instanceof LightningBolt ||
                        entity instanceof Marker ||
                        entity instanceof PartEntity ||
                        entity instanceof PrimedTnt ||
                        entity instanceof Projectile ||
                        entity instanceof ArmorStand) {

                    CompoundTag entityNBT = new CompoundTag();
                    entity.save(entityNBT);

                    if (EntityType.byString(entityNBT.getString("id")).isPresent()) {
                        entityList.add(entityNBT);
                        entityDimensions.add(serverLevel.dimension());
                    }
                }
            }
        }

        data.setEntityAggroTargets(entityAggroTargets);
        data.setEntityDataWithDimensions(entityList, entityDimensions);
        List<CompoundTag> groundItemsList = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                CompoundTag itemNBT = new CompoundTag();
                itemEntity.save(itemNBT);
                groundItemsList.add(itemNBT);
            }
        }
        data.setGroundItems(groundItemsList);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        logger.debug("Saving states took {} ms", durationMs);
        logger.info("Checkpoint set");

    }

    public static void restoreCheckpoint(ServerPlayer anchorPlayer) {
        Logger logger = LogManager.getLogger();
        logger.debug("Restoring checkpoint...");

        long startTime = System.nanoTime();
        try {
            ServerLevel level = anchorPlayer.serverLevel();
            CheckpointData data = CheckpointData.get(level);
            WorldData worldData = data.getWorldData();
            HolderLookup.Provider lookupProvider = level.registryAccess();

            if (data.getPlayerData(data.getAnchorPlayerUUID(), lookupProvider) == null) {
                logger.error("Player data is null!");
                return;
            }
            level.setDayTime(worldData.getDayTime());

            if (level.getLevelData() instanceof ServerLevelData serverData) {
                serverData.setGameTime(worldData.getGameTime());
                if (serverData instanceof PrimaryLevelData primaryData) {
                    primaryData.setGameTime(worldData.getGameTime());
                }
            }

            if (level.getLevelData() instanceof ServerLevelData serverData) {
                if (serverData instanceof PrimaryLevelData primaryData) {
                    primaryData.setRaining(worldData.isRaining());
                    primaryData.setThundering(worldData.isThundering());
                }
                serverData.setClearWeatherTime(worldData.getClearTime());


                if (worldData.isRaining()) {
                    serverData.setRainTime(worldData.getRainTime());
                    level.setRainLevel(1.0F);
                } else {
                    serverData.setRainTime(0);
                    level.setRainLevel(0);
                }

                if (worldData.isThundering()) {
                    serverData.setThunderTime(worldData.getThunderTime());
                    level.setThunderLevel(1.0F);
                } else {
                    serverData.setThunderTime(0);
                    level.setThunderLevel(0);
                }
            }

            for (ServerPlayer player : level.players()) {
                player.connection.send(new ClientboundGameEventPacket(
                        worldData.isRaining() ? ClientboundGameEventPacket.START_RAINING : ClientboundGameEventPacket.STOP_RAINING,
                        0.0F
                ));
                player.connection.send(new ClientboundGameEventPacket(
                        ClientboundGameEventPacket.RAIN_LEVEL_CHANGE,
                        worldData.isRaining() ? 1.0F : 0.0F
                ));

                player.connection.send(new ClientboundGameEventPacket(
                        ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE,
                        worldData.isThundering() ? 1.0F : 0.0F
                ));
            }
            for (BlockPos pos : worldData.modifiedBlocks) {
                int dimIndex = worldData.blockDimensionIndices.get(pos);
                ServerLevel dimLevel = level.getServer().getLevel(WorldData.getDimensionFromIndex(dimIndex));

                if (dimLevel != null) {
                    BlockState currentState = dimLevel.getBlockState(pos);
                    if (!currentState.isAir()) {
                        dimLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            for (Map.Entry<BlockPos, BlockState> entry : worldData.minedBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState originalState = entry.getValue();
                int dimIndex = worldData.blockDimensionIndices.get(pos);
                ServerLevel dimLevel = level.getServer().getLevel(WorldData.getDimensionFromIndex(dimIndex));

                if (dimLevel != null) {
                    BlockState currentState = dimLevel.getBlockState(pos);
                    if (currentState.isAir()) {
                        dimLevel.setBlock(pos, originalState, 2);
                    }
                }
            }

            for (BlockPos pos : worldData.modifiedFluidBlocks) {


                if (!worldData.blockDimensionIndices.containsKey(pos)) {
                    logger.info("No dimension index for modified fluid block at " + pos);
                    continue;
                }

                logger.debug("Restoring fluid block at " + pos);

                int dimIndex = worldData.blockDimensionIndices.get(pos);
                ServerLevel dimLevel = level.getServer().getLevel(WorldData.getDimensionFromIndex(dimIndex));
                if (dimLevel != null) {
                    BlockState currentState = dimLevel.getBlockState(pos);
                    if (!currentState.isAir()) {
                        dimLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            for (Map.Entry<BlockPos, BlockState> entry : worldData.minedFluidBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState originalState = entry.getValue();

                if (!worldData.blockDimensionIndices.containsKey(pos)) {
                    logger.info("No dimension index for modified fluid block at " + pos);
                    continue;
                }

                int dimIndex = worldData.blockDimensionIndices.get(pos);
                ServerLevel dimLevel = level.getServer().getLevel(WorldData.getDimensionFromIndex(dimIndex));
                if (dimLevel != null) {
                    BlockState currentState = dimLevel.getBlockState(pos);
                    if (currentState.isAir()) {
                        dimLevel.setBlock(pos, originalState, 3);
                    }
                }
            }


            int totalSaved = 0;
            int updateCount = 0;
            Map<ChunkPos, List<WorldData.SavedBlock>> savedBlocksByChunk = worldData.getSavedBlocksByChunk();

            for (Map.Entry<ChunkPos, List<WorldData.SavedBlock>> entry : savedBlocksByChunk.entrySet()) {
                List<WorldData.SavedBlock> savedBlocks = entry.getValue();
                totalSaved += savedBlocks.size();
                ResourceKey<Level> dimension = savedBlocks.get(0).dimension();
                ServerLevel dimLevel = level.getServer().getLevel(dimension);

                if (dimLevel == null) continue;

                for (WorldData.SavedBlock saved : savedBlocks) {
                    BlockState currentState = dimLevel.getBlockState(saved.pos());
                    if (!currentState.getBlock().equals(saved.state().getBlock())) {
                        updateCount++;
                        dimLevel.setBlock(saved.pos(), saved.state(), 2);
                    }
                }
            }
            for (Map.Entry<BlockPos, CompoundTag> entry : worldData.getBlockEntityData().entrySet()) {
                BlockPos pos = entry.getKey();
                CompoundTag blockEntityNbt = entry.getValue();

                Integer dimIndexInt = worldData.getInstanceBlockDimensionIndices().get(pos);
                if (dimIndexInt == null) {
                    logger.warn("No dimension index found for BlockEntity at {}. Skipping restoration.", pos);
                    continue;
                }
                ServerLevel dimLevel = level.getServer().getLevel(WorldData.getDimensionFromIndex(dimIndexInt));

                if (dimLevel != null) {
                    BlockEntity blockEntity = dimLevel.getBlockEntity(pos);
                    if (blockEntity != null) {
                        blockEntity.loadWithComponents(blockEntityNbt, lookupProvider);

                        blockEntity.setChanged();
                    } else {
                        logger.warn("No BlockEntity found at {} in dimension {} during restoration, though NBT was saved.",
                                pos, dimLevel.dimension().location());
                    }
                } else {
                    logger.warn("Dimension level for index {} (BlockEntity at {}) not found during restoration.", dimIndexInt, pos);
                }
            }
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                PlayerData pdata = data.getPlayerData(player.getUUID(), lookupProvider);
                if (pdata != null) {
                    if (!player.level().dimension().equals(pdata.dimension)) {
                        ServerLevel targetLevel = player.getServer().getLevel(pdata.dimension);
                        if (targetLevel != null) {
                            player.setHealth(pdata.health);
                            player.teleportTo(targetLevel, pdata.posX, pdata.posY, pdata.posZ, pdata.yaw, pdata.pitch);
                        }
                    } else {
                        player.setHealth(pdata.health);
                        ServerLevel targetLevel = player.getServer().getLevel(pdata.dimension);
                        player.teleportTo(targetLevel, pdata.posX, pdata.posY, pdata.posZ, pdata.yaw, pdata.pitch);

                    }
                    player.getFoodData().setFoodLevel(pdata.hunger);
                    player.setExperienceLevels(pdata.experienceLevel);
                    player.experienceProgress = pdata.experienceProgress;


                    player.setRemainingFireTicks(pdata.fireTicks);
                    player.setAirSupply(pdata.airSupply);
                    if (pdata.gameMode != null) {
                        switch (pdata.gameMode.toLowerCase()) {
                            case "survival" -> player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                            case "creative" -> player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                            case "adventure" -> player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
                            case "spectator" -> player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                        }
                    }


                    if (pdata.spawnDimension != null) {
                        player.setRespawnPosition(
                                pdata.spawnDimension,
                                new BlockPos((int)pdata.spawnX, (int)pdata.spawnY, (int)pdata.spawnZ),
                                pdata.yaw,
                                true,
                                false
                        );
                    }



                    player.setDeltaMovement(new Vec3(pdata.motionX, pdata.motionY, pdata.motionZ));
                    player.fallDistance = pdata.fallDistance;
                    player.removeAllEffects();
                    for (MobEffectInstance effect : pdata.potionEffects) {
                        player.addEffect(new MobEffectInstance(effect));
                    }
                    CompoundTag savedAdvTag = pdata.advancements;
                    ServerAdvancementManager advManager = level.getServer().getAdvancements();
                    for (AdvancementHolder advancementHolder : advManager.getAllAdvancements()) {
                        AdvancementProgress currentProgress = player.getAdvancements().getOrStartProgress(advancementHolder);

                        CompoundTag savedProgressTagForThisAdv = null;
                        String advKey = advancementHolder.id().toString();
                        if (savedAdvTag.contains(advKey)) {
                            savedProgressTagForThisAdv = savedAdvTag.getCompound(advKey);
                        }
                        Advancement advancementInstance = advancementHolder.value();
                        for (String criterionName : advancementInstance.criteria().keySet()) {
                            boolean wasCompletedInSave = savedProgressTagForThisAdv != null && savedProgressTagForThisAdv.getBoolean(criterionName);
                            boolean isCurrentlyCompleted = false;
                            if (currentProgress.getCriterion(criterionName) != null) {
                                isCurrentlyCompleted = currentProgress.getCriterion(criterionName).isDone();
                            }


                            if (isCurrentlyCompleted && !wasCompletedInSave) {
                                player.getAdvancements().revoke(advancementHolder, criterionName);
                                logger.trace("Revoked advancement criterion '{}' for {} on player {}", criterionName, advKey, player.getName().getString());
                            } else if (!isCurrentlyCompleted && wasCompletedInSave) {
                                player.getAdvancements().award(advancementHolder, criterionName);
                                logger.trace("Awarded advancement criterion '{}' for {} on player {}", criterionName, advKey, player.getName().getString());
                            }
                        }
                    }
                    Inventory playerInventory = player.getInventory();
                    playerInventory.clearContent();

                    ListTag savedNbt = pdata.inventoryNBT;
                    logger.info("Restoring inventory for {}. Received ListTag with {} entries.",
                            player.getName().getString(), savedNbt.size());

                    if (savedNbt.isEmpty()) {
                        logger.warn("The saved inventory NBT for {} was empty. Nothing to restore.", player.getName().getString());
                    }

                    for (int i = 0; i < savedNbt.size(); ++i) {
                        CompoundTag itemTag = savedNbt.getCompound(i);
                        int slot = itemTag.getByte("Slot") & 255;

                        logger.debug("  -> Processing item tag for slot {}: {}", slot, itemTag.toString());

                        if(lookupProvider == null) {
                            logger.error("CRITICAL: The HolderLookup.Provider is NULL during restore! ItemStack.parse will fail.");
                        }
                        Optional<ItemStack> parsedStackOptional = ItemStack.parse(lookupProvider, itemTag);

                        if (parsedStackOptional.isPresent()) {
                            ItemStack itemstack = parsedStackOptional.get();
                            if (!itemstack.isEmpty()) {
                                logger.info("  Successfully parsed item: {} for slot {}", itemstack, slot);

                                if (slot >= 0 && slot < playerInventory.items.size()) {
                                    playerInventory.items.set(slot, itemstack);
                                    logger.debug("    -> Placed in main inventory at index {}.", slot);
                                } else if (slot >= 100 && slot < playerInventory.armor.size() + 100) {
                                    playerInventory.armor.set(slot - 100, itemstack);
                                    logger.debug("    -> Placed in armor inventory at index {}.", slot - 100);
                                } else if (slot >= 150 && slot < playerInventory.offhand.size() + 150) {
                                    playerInventory.offhand.set(slot - 150, itemstack);
                                    logger.debug("    -> Placed in offhand inventory at index {}.", slot - 150);
                                } else {
                                    logger.warn("    -> Parsed item for an invalid slot number: {}. Item will be lost.", slot);
                                }
                            } else {
                                logger.warn("  ItemStack.parse resulted in an EMPTY stack for slot {}. Original tag: {}", slot, itemTag);
                            }
                        } else {
                            logger.error("  FAILED to parse ItemStack from NBT for slot {}. Original tag: {}", slot, itemTag);
                        }
                    }




                }
            }
            List<Entity> entitiesToRemove = new ArrayList<>();
            for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    if (!(entity instanceof ServerPlayer) && !entity.isRemoved()) {
                        entitiesToRemove.add(entity);
                    }
                }
                for (Entity entity : entitiesToRemove) {
                    entity.discard();
                }
                entitiesToRemove.clear();
            }

            List<CompoundTag> entities = data.getEntityData();
            List<ResourceKey<Level>> entityDimensions = data.getEntityDimensions();
            Map<UUID, UUID> entityAggroTargets = data.getEntityAggroTargets();
            Map<UUID, Mob> restoredMobs = new HashMap<>();
            for (int i = 0; i < entities.size(); i++) {
                CompoundTag eNBT = entities.get(i);
                ResourceKey<Level> entityDim = entityDimensions.get(i);

                ServerLevel targetLevel = level.getServer().getLevel(entityDim);
                if (targetLevel != null) {
                    EntityType.loadEntityRecursive(eNBT, targetLevel, (entity) -> {
                        if (entity != null) {
                            targetLevel.addFreshEntity(entity);
                            if (entity instanceof Mob mob) {
                                restoredMobs.put(mob.getUUID(), mob);
                            }
                        }
                        return entity;
                    });
                }
            }
            for (Map.Entry<UUID, UUID> entry : entityAggroTargets.entrySet()) {
                UUID mobUUID = entry.getKey();
                UUID targetUUID = entry.getValue();

                if (restoredMobs.containsKey(mobUUID) && restoredMobs.containsKey(targetUUID)) {
                    Mob mob = restoredMobs.get(mobUUID);
                    Entity target = restoredMobs.get(targetUUID);

                    if (target instanceof Mob || target instanceof ServerPlayer) {
                        mob.setTarget((Mob) target);
                    }
                }
            }
            List<CompoundTag> groundItemsList = data.getGroundItems();
            if (groundItemsList != null) {
                for (CompoundTag itemNBT : groundItemsList) {
                    EntityType.loadEntityRecursive(itemNBT, level, (entity) -> {
                        if (entity instanceof ItemEntity) {
                            level.addFreshEntity(entity);
                        }
                        return entity;
                    });
                }
            }

            for (BlockPos eyePos : worldData.addedEyes) {
                BlockState state = level.getBlockState(eyePos);
                if (state.getBlock() == Blocks.END_PORTAL_FRAME && state.getValue(EndPortalFrameBlock.HAS_EYE)) {
                    level.setBlock(eyePos, state.setValue(EndPortalFrameBlock.HAS_EYE, false), 3);
                }
            }








            List<WorldData.LightningStrike> strikes = worldData.getSavedLightnings();
            for (WorldData.LightningStrike strike : strikes) {

                long delay = strike.tickTime - level.getGameTime();
                if (delay < 0) delay = 1;

                LightningScheduler.schedule(level, strike.pos, strike.tickTime);

            }
            for (BlockPos firePos : worldData.getNewFires()) {
                if (level.getBlockState(firePos).getBlock() == Blocks.FIRE) {
                    level.setBlockAndUpdate(firePos, Blocks.AIR.defaultBlockState());
                }
            }



            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            logger.debug("Restoring states took {} ms", durationMs);


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }


}