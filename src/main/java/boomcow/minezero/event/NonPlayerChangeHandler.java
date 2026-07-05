package boomcow.minezero.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NonPlayerChangeHandler {

    private static final Logger LOGGER = LogManager.getLogger(NonPlayerChangeHandler.class);

    /**
     * On Forge/NeoForge this handler tracks non-player world changes (fire
     * placement, explosion block damage, lethal-explosion anchor handling).
     * Fabric API exposes no equivalent events, so this needs mixins into
     * FireBlock/Explosion. Not yet ported.
     */
    public static void register() {
        LOGGER.info("Non-player world change tracking is not yet ported to Fabric (requires mixins).");
    }
}
