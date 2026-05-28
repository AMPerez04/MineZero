package boomcow.minezero.event;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NonPlayerChangeHandler {

    private static WorldData getActiveWorldData(ServerLevel level) {
        if (level == null || level.getServer() == null) return null;
        CheckpointData checkpointData = CheckpointData.get(level);
        return checkpointData.getWorldData();
    }

    @SubscribeEvent
    public static void onFirePlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Logger logger = LogManager.getLogger();
        if (event.getPlacedBlock().getBlock() == Blocks.FIRE) {
            CheckpointData data = CheckpointData.get(level);
            long now = level.getGameTime();
            if (now > data.getWorldData().getCheckpointTick()) {
                data.getWorldData().getNewFires().add(event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Logger logger = LogManager.getLogger();

        CheckpointData data = CheckpointData.get(level);
        WorldData worldDataInstance = getActiveWorldData(level);

        long now = level.getGameTime();
        int dimensionIndex = WorldData.getDimensionIndex(level.dimension());

        for (BlockPos pos : event.getAffectedBlocks()) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || now <= data.getWorldData().getCheckpointTick()) continue;

            List<BlockPos> affectedPositions = new ArrayList<>();
            affectedPositions.add(pos);

            Block block = state.getBlock();
            if (block instanceof DoorBlock) {
                if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                    affectedPositions.add(pos.above());
                } else {
                    affectedPositions.add(pos.below());
                }
            }
            else if (block instanceof BedBlock) {
                Direction facing = state.getValue(BedBlock.FACING);
                if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
                    affectedPositions.add(pos.relative(facing));
                } else {
                    affectedPositions.add(pos.relative(facing.getOpposite()));
                }
            }
            for (BlockPos currentPos : affectedPositions) {
                if (worldDataInstance.modifiedBlocks.contains(currentPos)) {
                    worldDataInstance.modifiedBlocks.remove(currentPos);
                } else {
                    BlockState currentState = level.getBlockState(currentPos);
                    worldDataInstance.minedBlocks.put(currentPos, currentState);
                    worldDataInstance.blockDimensionIndices.put(currentPos, dimensionIndex);
                }
            }
        }
    }

    public static float getExplosionRadius(Explosion explosion) {
        try {
            Field radiusField = Explosion.class.getDeclaredField("radius");
            radiusField.setAccessible(true);
            return radiusField.getFloat(explosion);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return 0.0f;
        }
    }


    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        CheckpointData data = CheckpointData.get(level);
        UUID anchorUUID = data.getAnchorPlayerUUID();
        if (anchorUUID == null) return;

        ServerPlayer anchorPlayer = level.getServer().getPlayerList().getPlayer(anchorUUID);
        if (anchorPlayer == null || anchorPlayer.isDeadOrDying()) return;

        Explosion explosion = event.getExplosion();
        Vec3 explosionPos = explosion.center();
        float explosionRadius = getExplosionRadius(explosion);

        double distance = anchorPlayer.position().distanceTo(explosionPos);
        float seenPercent = Explosion.getSeenPercent(explosionPos, anchorPlayer);

        float scaled = (1.0F - (float)(distance / (explosionRadius * 2.0F))) * seenPercent;
        float estimatedDamage = (scaled * scaled + scaled) * explosionRadius * 7.0F + 1.0F;

        Logger logger = LogManager.getLogger();
        logger.info("Explosion at {} | distance: {} | seen: {} | estimated dmg: {}",
                explosionPos,
                String.format("%.2f", distance),
                String.format("%.2f", seenPercent),
                String.format("%.2f", estimatedDamage));



        if (estimatedDamage >= anchorPlayer.getHealth()) {
            event.setCanceled(true);
            anchorPlayer.hurt(level.damageSources().explosion(anchorPlayer, null), anchorPlayer.getMaxHealth()*5);
        }
    }


    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        CheckpointData data = CheckpointData.get(level);
        if (data.getAnchorPlayerUUID() == null) return;

        WorldData worldData = data.getWorldData();
        if (worldData == null) return;

        int dimIndex = WorldData.getDimensionIndex(level.dimension());

        if (!(event.getChunk() instanceof LevelChunk levelChunk)) return;

        for (BlockEntity be : levelChunk.getBlockEntities().values()) {
            if (be == null || be.isRemoved()) continue;
            CompoundTag nbt = be.saveWithFullMetadata(level.registryAccess());
            if (nbt.contains("LootTable", Tag.TAG_STRING)) {
                BlockPos pos = be.getBlockPos().immutable();
                worldData.saveBlockEntity(pos, nbt);
                worldData.blockDimensionIndices.put(pos, dimIndex);
            }
        }
    }
}
