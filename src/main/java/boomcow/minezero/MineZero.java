package boomcow.minezero;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import boomcow.minezero.checkpoint.PlayerData;
import boomcow.minezero.command.RestoreCheckpointCommand;
import boomcow.minezero.command.SetAnchorCommand;
import boomcow.minezero.command.SetCheckpointCommand;
import boomcow.minezero.event.DeathEventHandler;
import boomcow.minezero.input.KeyBindings;
import boomcow.minezero.items.ArtifactFluteItem;
import boomcow.minezero.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Locale;

@Mod(MineZero.MODID)
public class MineZero {

    public static final String MODID = "minezero";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)));

    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
            () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> ARTIFACT_FLUTE = ITEMS.register("artifact_flute",
            () -> new ArtifactFluteItem(new Item.Properties().stacksTo(1)));

    public MineZero() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::setupNetworking);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DeathEventHandler());

        ModSoundEvents.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SetCheckpointCommand.register(event.getDispatcher());
        SetAnchorCommand.register(event.getDispatcher());
        RestoreCheckpointCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        LOGGER.info("Loading custom game rules: autoCheckpointEnabled = {}", ModGameRules.AUTO_CHECKPOINT_ENABLED);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ARTIFACT_FLUTE.get());
        }
    }

    private void setupNetworking(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
        });
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        LOGGER.info("Loading MineZero config");
        ConfigHandler.loadConfig(configEvent.getConfig());
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        LOGGER.info("Reloading MineZero config");
        ConfigHandler.loadConfig(configEvent.getConfig());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        if (player.level().isClientSide())
            return;

        ServerLevel level = player.serverLevel();
        CheckpointData data = CheckpointData.get(level);

        LOGGER.info("[MineZero][LOGIN] Player {} (UUID: {}) logged in.", player.getName().getString(),
                player.getUUID());

        if (data.getAnchorPlayerUUID() == null && data.getPlayerData(player.getUUID()) == null) {
            LOGGER.info(
                    "[MineZero][LOGIN] No anchor player set and player {} is new to checkpoint. Setting initial checkpoint for this player.",
                    player.getName().getString());

            if (player.level().getGameRules().getBoolean(ModGameRules.CHECKPOINT_ON_WORLD_CREATION)) {
                CheckpointManager.setCheckpoint(player);
            }

            LOGGER.info("[MineZero][LOGIN] Initial anchor and checkpoint set for {}", player.getName().getString());
        }

        else if (data.getPlayerData(player.getUUID()) == null) {
            LOGGER.info("[MineZero][LOGIN] Player {} (UUID: {}) not found in current checkpoint data. Adding them now.",
                    player.getName().getString(), player.getUUID());

            PlayerData pDataForNewPlayer = new PlayerData();

            pDataForNewPlayer.posX = player.getX();
            pDataForNewPlayer.posY = player.getY();
            pDataForNewPlayer.posZ = player.getZ();
            pDataForNewPlayer.yaw = player.getYRot();
            pDataForNewPlayer.pitch = player.getXRot();
            pDataForNewPlayer.dimension = player.level().dimension();
            pDataForNewPlayer.gameMode = player.gameMode.getGameModeForPlayer().getName().toLowerCase(Locale.ROOT);
            pDataForNewPlayer.motionX = player.getDeltaMovement().x;
            pDataForNewPlayer.motionY = player.getDeltaMovement().y;
            pDataForNewPlayer.motionZ = player.getDeltaMovement().z;
            pDataForNewPlayer.fallDistance = player.fallDistance;
            pDataForNewPlayer.health = player.getHealth();
            pDataForNewPlayer.hunger = player.getFoodData().getFoodLevel();
            pDataForNewPlayer.experienceLevel = player.experienceLevel;
            pDataForNewPlayer.experienceProgress = player.experienceProgress;
            pDataForNewPlayer.fireTicks = player.getRemainingFireTicks();

            BlockPos spawn = player.getRespawnPosition();
            ResourceKey<Level> spawnDim = player.getRespawnDimension();
            if (spawn != null && spawnDim != null) {
                pDataForNewPlayer.spawnX = spawn.getX() + 0.5;
                pDataForNewPlayer.spawnY = spawn.getY();
                pDataForNewPlayer.spawnZ = spawn.getZ() + 0.5;
                pDataForNewPlayer.spawnDimension = spawnDim;
                pDataForNewPlayer.spawnForced = player.isRespawnForced();
            }

            pDataForNewPlayer.potionEffects.clear();
            for (MobEffectInstance effect : player.getActiveEffects()) {
                pDataForNewPlayer.potionEffects.add(new MobEffectInstance(effect));
            }

            pDataForNewPlayer.inventory.clear();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                pDataForNewPlayer.inventory.add(player.getInventory().getItem(i).copy());
            }

            CompoundTag advTag = new CompoundTag();
            if (player.server != null) {
                for (Advancement advancement : player.server.getAdvancements().getAllAdvancements()) {
                    AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                    CompoundTag progressTag = new CompoundTag();
                    for (String criterion : progress.getCompletedCriteria()) {
                        progressTag.putBoolean(criterion, true);
                    }
                    advTag.put(advancement.getId().toString(), progressTag);
                }
            }
            pDataForNewPlayer.advancements = advTag;

            data.savePlayerData(player.getUUID(), pDataForNewPlayer);
            LOGGER.info("[MineZero][LOGIN] Player {} added to checkpoint with their current state.",
                    player.getName().getString());
        } else {

            LOGGER.info("[MineZero][LOGIN] Player {} (UUID: {}) already has data in the checkpoint.",
                    player.getName().getString(), player.getUUID());
            LOGGER.info("[MineZero][LOGIN] Player UUID = {}, Stored Anchor = {}",
                    player.getUUID(), data.getAnchorPlayerUUID());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            LOGGER.info("Registering MineZero Key Mappings");
            event.register(KeyBindings.EXAMPLE_ACTION_KEY);
            event.register(KeyBindings.SELF_DAMAGE_KEY);
        }
    }
}
