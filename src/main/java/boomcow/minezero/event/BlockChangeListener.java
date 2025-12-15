package boomcow.minezero.event;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class BlockChangeListener {
    private static final Logger LOGGER_BCL = LogManager.getLogger("MineZeroBCL");

    private static WorldData getActiveWorldData(ServerLevel level) {
        if (level == null || level.getServer() == null)
            return null;
        CheckpointData checkpointData = CheckpointData.get(level);
        return checkpointData.getWorldData();
    }

    @SubscribeEvent
    public static void onSolidBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            WorldData worldDataInstance = getActiveWorldData(level);

            if (worldDataInstance == null) {
                LOGGER_BCL.warn("Could not get active WorldData for block placement tracking.");
                return;
            }

            BlockState newState = event.getState();
            BlockPos pos = event.getPos().immutable();

            if (newState.getBlock() instanceof LiquidBlock)
                return;

            if (newState.getBlock() == Blocks.END_PORTAL_FRAME && newState.getValue(EndPortalFrameBlock.HAS_EYE)) {
                worldDataInstance.getAddedEyes().add(pos);
                return;
            }

            LOGGER_BCL.info("Block placed at: " + pos + " tracking in instance WorldData.");
            worldDataInstance.getModifiedBlocks().add(pos);
            worldDataInstance.getInstanceBlockDimensionIndices().put(pos,
                    WorldData.getDimensionIndex(level.dimension()));
        }
    }

    @SubscribeEvent
    public static void onSolidBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            WorldData worldDataInstance = getActiveWorldData(level);

            if (worldDataInstance == null) {
                LOGGER_BCL.warn("Could not get active WorldData for block break tracking.");
                return;
            }

            BlockState state = event.getState();
            if (!(state.getBlock() instanceof LiquidBlock)) {

                List<BlockPos> brokenBlockPositions = new ArrayList<>();
                BlockPos pos = event.getPos().immutable();
                brokenBlockPositions.add(pos);
                Block block = state.getBlock();

                if (block instanceof DoorBlock) {
                    if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                        brokenBlockPositions.add(pos.above());
                    } else {
                        brokenBlockPositions.add(pos.below());
                    }
                } else if (block instanceof BedBlock) {
                    Direction facing = state.getValue(BedBlock.FACING);
                    if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
                        brokenBlockPositions.add(pos.relative(facing));
                    } else {
                        brokenBlockPositions.add(pos.relative(facing.getOpposite()));
                    }
                }

                for (BlockPos currentPos : brokenBlockPositions) {
                    if (worldDataInstance.getModifiedBlocks().contains(currentPos)) {
                        worldDataInstance.getModifiedBlocks().remove(currentPos);
                    } else {
                        BlockState currentState = level.getBlockState(currentPos);
                        worldDataInstance.getMinedBlocks().put(currentPos, currentState);
                        worldDataInstance.getInstanceBlockDimensionIndices().put(currentPos,
                                WorldData.getDimensionIndex(level.dimension()));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBucketRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ServerLevel level = (ServerLevel) event.getLevel();
        WorldData worldDataInstance = getActiveWorldData(level);

        if (worldDataInstance == null) {
            LOGGER_BCL.warn("Could not get active WorldData for bucket interaction tracking.");
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BucketItem bucketItem) {
            BlockPos clickedPos = event.getPos().immutable();
            BlockState clickedState = level.getBlockState(clickedPos);
            
            // Default target is adjacent, but if clicked block is waterloggable and we have liquid, use clickedPos
            BlockPos targetPos = clickedPos.relative(event.getFace()).immutable();
            
            boolean isWaterloggable = clickedState.getBlock() instanceof SimpleWaterloggedBlock 
                && clickedState.hasProperty(BlockStateProperties.WATERLOGGED)
                && !clickedState.getValue(BlockStateProperties.WATERLOGGED);

            if (isWaterloggable && (bucketItem.getFluid() == Fluids.WATER || bucketItem.getFluid() == Fluids.LAVA)) {
                 targetPos = clickedPos;
            }

            if (bucketItem.getFluid() == Fluids.LAVA || bucketItem.getFluid() == Fluids.WATER) {
                worldDataInstance.getModifiedFluidBlocks().add(targetPos);
                worldDataInstance.getInstanceBlockDimensionIndices().put(targetPos,
                        WorldData.getDimensionIndex(level.dimension()));
                LOGGER_BCL.info("Fluid placed at: " + targetPos + " tracking in instance WorldData.");
            } else if (bucketItem.getFluid() == Fluids.EMPTY) {

                BlockState fluidStateSource = level.getBlockState(clickedPos);
                BlockPos actualFluidPos = null;

                if (fluidStateSource.getFluidState().isSourceOfType(Fluids.WATER)
                        || fluidStateSource.getFluidState().isSourceOfType(Fluids.LAVA)) {
                    actualFluidPos = clickedPos;
                } else {

                    BlockState adjacentState = level.getBlockState(targetPos);
                    if (adjacentState.getFluidState().isSourceOfType(Fluids.WATER)
                            || adjacentState.getFluidState().isSourceOfType(Fluids.LAVA)) {
                        actualFluidPos = targetPos;
                    }
                }

                if (actualFluidPos == null && (level.getBlockState(event.getPos()).getFluidState().is(Fluids.WATER)
                        || level.getBlockState(event.getPos()).getFluidState().is(Fluids.LAVA))) {
                    actualFluidPos = event.getPos();
                }

                if (actualFluidPos != null) {
                    BlockState stateToMine = level.getBlockState(actualFluidPos);
                    if (worldDataInstance.getModifiedFluidBlocks().contains(actualFluidPos)) {
                        worldDataInstance.getModifiedFluidBlocks().remove(actualFluidPos);
                    } else {
                        worldDataInstance.getMinedFluidBlocks().put(actualFluidPos, stateToMine);
                        worldDataInstance.getInstanceBlockDimensionIndices().put(actualFluidPos,
                                WorldData.getDimensionIndex(level.dimension()));
                    }
                    LOGGER_BCL.info("Fluid picked up from: " + actualFluidPos + " tracking in instance WorldData.");
                }
            }
        }
    }
}
