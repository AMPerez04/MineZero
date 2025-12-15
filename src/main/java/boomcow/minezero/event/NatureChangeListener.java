package boomcow.minezero.event;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class NatureChangeListener {

    private static WorldData getActiveWorldData(ServerLevel level) {
        if (level == null || level.getServer() == null)
            return null;
        CheckpointData checkpointData = CheckpointData.get(level);
        if (checkpointData.getAnchorPlayerUUID() == null)
            return null;
        return checkpointData.getWorldData();
    }

    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldData wd = getActiveWorldData(level);
            if (wd != null) {
                wd.trackBlockBeforeChange(level, event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onFluidForm(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldData wd = getActiveWorldData(level);
            if (wd != null) {
                wd.trackBlockBeforeChange(level, event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldData wd = getActiveWorldData(level);
            if (wd != null) {
                wd.trackBlockBeforeChange(level, event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onPistonMove(PistonEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldData wd = getActiveWorldData(level);
            if (wd != null) {
                wd.trackBlockBeforeChange(level, event.getPos());
                
                PistonStructureResolver helper = event.getStructureHelper();
                if (helper != null && helper.resolve()) {
                    for (BlockPos pos : helper.getToPush()) {
                         wd.trackBlockBeforeChange(level, pos);
                    }
                    for (BlockPos pos : helper.getToDestroy()) {
                         wd.trackBlockBeforeChange(level, pos);
                    }
                }
            }
        }
    }
}
