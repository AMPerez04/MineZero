package boomcow.minezero.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LightningScheduler {
    private static final List<LightningTask> scheduledTasks = new ArrayList<>();

    public static void schedule(ServerLevel level, BlockPos pos, long targetTickTime) {
        synchronized (scheduledTasks) {
            scheduledTasks.add(new LightningTask(level, pos, targetTickTime));
        }
    }

    /**
     * Called every server tick for each ServerLevel where lightning strikes
     * might be scheduled.
     */
    public static void tick(ServerLevel level) {
        long currentTick = level.getGameTime();
        synchronized (scheduledTasks) {
            Iterator<LightningTask> iterator = scheduledTasks.iterator();
            while (iterator.hasNext()) {
                LightningTask task = iterator.next();
                if (task.level == level && task.targetTickTime <= currentTick) {
                    LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (lightningBolt != null) {
                        lightningBolt.setPos(task.pos.getX() + 0.5, task.pos.getY(), task.pos.getZ() + 0.5);
                        level.addFreshEntity(lightningBolt);
                    }
                    iterator.remove();
                }
            }
        }
    }

    private static class LightningTask {
        final ServerLevel level;
        final BlockPos pos;
        final long targetTickTime;

        LightningTask(ServerLevel level, BlockPos pos, long targetTickTime) {
            this.level = level;
            this.pos = pos;
            this.targetTickTime = targetTickTime;
        }
    }
}
