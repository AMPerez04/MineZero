package boomcow.minezero;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import boomcow.minezero.checkpoint.PlayerData;
import boomcow.minezero.command.SetCheckpointCommand;
import boomcow.minezero.command.SetAnchorCommand;
import boomcow.minezero.command.RestoreCheckpointCommand;
import boomcow.minezero.event.DeathEventHandler;
import boomcow.minezero.items.ArtifactFluteItem;
import boomcow.minezero.network.PacketHandler;
import boomcow.minezero.proxy.CommonProxy;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

@Mod(modid = MineZero.MODID, name = MineZero.NAME, version = MineZero.VERSION)
public class MineZero {

    public static final String MODID = "minezero";
    public static final String NAME = "MineZero";
    public static final String VERSION = "1.0.9";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    // SRG field names for 1.12.2 private fields
    private static final String FIELD_ENTITY_FIRE = "field_70151_c";
    private static final String FIELD_PLAYER_SPAWN_DIMENSION = "field_71078_a";

    @Mod.Instance(MODID)
    public static MineZero instance;

    @SidedProxy(clientSide = "boomcow.minezero.proxy.ClientProxy", serverSide = "boomcow.minezero.proxy.CommonProxy")
    public static CommonProxy proxy;


    public static Item ARTIFACT_FLUTE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("HELLO FROM PRE-INIT");
        PacketHandler.register();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("HELLO FROM INIT");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DeathEventHandler());
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
        event.registerServerCommand(new SetCheckpointCommand());
        event.registerServerCommand(new SetAnchorCommand());
        event.registerServerCommand(new RestoreCheckpointCommand());
        ModGameRules.register(event.getServer().getWorld(0).getGameRules());
    }

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {

            ARTIFACT_FLUTE = new ArtifactFluteItem();
            ARTIFACT_FLUTE.setRegistryName("artifact_flute");
            ARTIFACT_FLUTE.setUnlocalizedName(MODID + ".artifact_flute");
            ARTIFACT_FLUTE.setCreativeTab(CreativeTabs.TOOLS);
            event.getRegistry().register(ARTIFACT_FLUTE);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (player.world.isRemote) return;

        WorldServer level = player.getServerWorld();
        CheckpointData data = CheckpointData.get(level);

        LOGGER.info("[MineZero][LOGIN] Player {} (UUID: {}) logged in.", player.getName(), player.getUniqueID());

        if (data.getAnchorPlayerUUID() == null && data.getPlayerData(player.getUniqueID()) == null) {
            LOGGER.info("[MineZero][LOGIN] No anchor player set and player {} is new. Setting initial checkpoint.", player.getName());
            if (player.world.getGameRules().getBoolean(ModGameRules.CHECKPOINT_ON_WORLD_CREATION)) {
                CheckpointManager.setCheckpoint(player);
            }
        }
        else if (data.getPlayerData(player.getUniqueID()) == null) {
            LOGGER.info("[MineZero][LOGIN] Player {} not found in checkpoint data. Adding now.", player.getName());

            PlayerData pDataForNewPlayer = new PlayerData();
            pDataForNewPlayer.posX = player.posX;
            pDataForNewPlayer.posY = player.posY;
            pDataForNewPlayer.posZ = player.posZ;
            pDataForNewPlayer.yaw = player.rotationYaw;
            pDataForNewPlayer.pitch = player.rotationPitch;
            pDataForNewPlayer.dimension = player.dimension;
            pDataForNewPlayer.gameMode = player.interactionManager.getGameType().getName().toLowerCase(Locale.ROOT);
            pDataForNewPlayer.motionX = player.motionX;
            pDataForNewPlayer.motionY = player.motionY;
            pDataForNewPlayer.motionZ = player.motionZ;
            pDataForNewPlayer.fallDistance = player.fallDistance;
            pDataForNewPlayer.health = player.getHealth();
            pDataForNewPlayer.hunger = player.getFoodStats().getFoodLevel();
            pDataForNewPlayer.experienceLevel = player.experienceLevel;
            pDataForNewPlayer.experienceProgress = player.experience;

            // FIX: Reflection for private field 'fire'
            try {
                pDataForNewPlayer.fireTicks = ObfuscationReflectionHelper.getPrivateValue(Entity.class, player, FIELD_ENTITY_FIRE);
            } catch (Exception e) {
                pDataForNewPlayer.fireTicks = 0;
            }

            BlockPos spawn = player.getBedLocation(player.dimension);
            if (spawn != null) {
                pDataForNewPlayer.spawnX = spawn.getX() + 0.5;
                pDataForNewPlayer.spawnY = spawn.getY();
                pDataForNewPlayer.spawnZ = spawn.getZ() + 0.5;

                // FIX: Reflection for private field 'spawnDimension'
                try {
                    pDataForNewPlayer.spawnDimension = ObfuscationReflectionHelper.getPrivateValue(EntityPlayer.class, player, FIELD_PLAYER_SPAWN_DIMENSION);
                } catch (Exception e) {
                    pDataForNewPlayer.spawnDimension = 0;
                }

                pDataForNewPlayer.spawnForced = player.isSpawnForced(player.dimension);
            }

            pDataForNewPlayer.potionEffects.clear();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                pDataForNewPlayer.potionEffects.add(new PotionEffect(effect));
            }

            pDataForNewPlayer.inventory.clear();
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                pDataForNewPlayer.inventory.add(player.inventory.getStackInSlot(i).copy());
            }

            NBTTagCompound advTag = new NBTTagCompound();
            // FIX: Resolve mcServer using FMLCommonHandler
            if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
                PlayerAdvancements advancements = player.getAdvancements();
                for (Advancement advancement : FMLCommonHandler.instance().getMinecraftServerInstance().getAdvancementManager().getAdvancements()) {
                    AdvancementProgress progress = advancements.getProgress(advancement);
                    if (progress.hasProgress()) {
                        NBTTagCompound progressTag = new NBTTagCompound();
                        for (String criterion : progress.getCompletedCriteria()) {
                            progressTag.setBoolean(criterion, true);
                        }
                        advTag.setTag(advancement.getId().toString(), progressTag);
                    }
                }
            }
            pDataForNewPlayer.advancements = advTag;

            data.savePlayerData(player.getUniqueID(), pDataForNewPlayer);
        }
    }
}