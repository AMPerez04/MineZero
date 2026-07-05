package boomcow.minezero.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExplosionEventHandler {

    private static final Logger LOGGER = LogManager.getLogger(ExplosionEventHandler.class);

    /**
     * On Forge/NeoForge this handler clears an explosion's affected block list
     * while the anchor player is dead so the blast can't desync the diff data.
     * Fabric API has no explosion events, so this needs a mixin into
     * Explosion#finalizeExplosion. Not yet ported.
     */
    public static void register() {
        LOGGER.info("Explosion tracking is not yet ported to Fabric (requires a mixin).");
    }
}
