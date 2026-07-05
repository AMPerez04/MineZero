package boomcow.minezero.event;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.WorldData;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BlockChangeListener {
    private static final Logger LOGGER_BCL = LogManager.getLogger("MineZeroBCL");

    private static WorldData getActiveWorldData(ServerLevel level) {
        if (level == null || level.getServer() == null) return null;
        CheckpointData checkpointData = CheckpointData.get(level);
        return checkpointData.getWorldData();
    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                onSolidBlockBreak((ServerLevel) level, serverPlayer, pos.immutable(), state);
            }
        });
    }

    private static void onSolidBlockBreak(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState brokenState) {
        WorldData worldDataInstance = getActiveWorldData(level);
        if (worldDataInstance == null) {
            LOGGER_BCL.warn("Could not get active WorldData for block break tracking.");
            return;
        }

        if (brokenState.getBlock() instanceof LiquidBlock) return;

        List<BlockPos> brokenBlockPositions = new ArrayList<>();
        brokenBlockPositions.add(pos);
        Block block = brokenState.getBlock();

        if (block instanceof DoorBlock) {
            if (brokenState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                brokenBlockPositions.add(pos.above());
            } else {
                brokenBlockPositions.add(pos.below());
            }
        } else if (block instanceof BedBlock) {
            Direction facing = brokenState.getValue(BedBlock.FACING);
            if (brokenState.getValue(BedBlock.PART) == BedPart.FOOT) {
                brokenBlockPositions.add(pos.relative(facing));
            } else {
                brokenBlockPositions.add(pos.relative(facing.getOpposite()));
            }
        }

        for (BlockPos currentPos : brokenBlockPositions) {
            if (worldDataInstance.getModifiedBlocks().contains(currentPos)) {
                worldDataInstance.getModifiedBlocks().remove(currentPos);
            } else {
                BlockState stateToStore = currentPos.equals(pos) ? brokenState : level.getBlockState(currentPos);
                worldDataInstance.getMinedBlocks().put(currentPos, stateToStore);
                worldDataInstance.getInstanceBlockDimensionIndices().put(currentPos, WorldData.getDimensionIndex(level.dimension()));
            }
        }
    }

    public static void onSolidBlockPlace(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState newState) {
        WorldData worldDataInstance = getActiveWorldData(level);
        if (worldDataInstance == null) {
            LOGGER_BCL.warn("Could not get active WorldData for block placement tracking.");
            return;
        }

        if (newState.getBlock() == Blocks.END_PORTAL_FRAME && newState.hasProperty(EndPortalFrameBlock.HAS_EYE) && newState.getValue(EndPortalFrameBlock.HAS_EYE)) {
            worldDataInstance.getAddedEyes().add(pos);
            return;
        }

        worldDataInstance.getModifiedBlocks().add(pos);
        worldDataInstance.getInstanceBlockDimensionIndices().put(pos, WorldData.getDimensionIndex(level.dimension()));
    }
}
