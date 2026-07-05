package boomcow.minezero.event;

import boomcow.minezero.checkpoint.CheckpointData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;

public class LightningStrikeListener {

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof LightningBolt)) return;

            CheckpointData data = CheckpointData.get(level);
            if (data != null && data.getWorldData() != null) {
                BlockPos strikePos = entity.blockPosition();
                long tickTime = level.getGameTime();
                data.getWorldData().addLightningStrike(strikePos, tickTime);
            }
        });
    }
}
